package com.indothai.order_service.runner;



import com.indothai.order_service.client.PositionServiceClient;
import com.indothai.order_service.config.OrderServiceConfig;
import com.indothai.order_service.parser.OrderCsvParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class OrderStreamRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OrderStreamRunner.class);

    private final OrderServiceConfig config;
    private final PositionServiceClient client;
    private final ResourceLoader resourceLoader;

    public OrderStreamRunner(OrderServiceConfig config,
                             PositionServiceClient client,
                             ResourceLoader resourceLoader) {
        this.config = config;
        this.client = client;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) {
        Resource resource = resourceLoader.getResource(config.getCsvPath());

        if (!resource.exists()) {
            log.error("CSV resource not found at location: {}", config.getCsvPath());
            return;
        }

        long minIntervalNanos = 1_000_000_000L / config.getRateLimit();
        long totalRows = 0;
        long validEvents = 0;
        long rejectedRows = 0;
        long sentEvents = 0;

        log.info("Streaming orders from: {} (Throttle: {} events/sec)", config.getCsvPath(), config.getRateLimit());

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8) ) ) {

            String line;
            long rowNum = 1;

            while ((line = reader.readLine()) != null) {
                rowNum++;
                totalRows++;

                try {
                    var parsedOpt = OrderCsvParser.parseAndValidateRow(line, rowNum);
                    if (parsedOpt.isPresent()) {
                        validEvents++;
                        var event = parsedOpt.get();

                        long startTime = System.nanoTime();
                        boolean success = client.sendEvent(config.getTargetUrl(), event);
                        if (success) {
                            sentEvents++;
                        }

                        // Rate limiting to 50 events/sec
                        long elapsedNanos = System.nanoTime() - startTime;
                        long sleepNanos = minIntervalNanos - elapsedNanos;
                        if (sleepNanos > 0) {
                            Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    rejectedRows++;
                    log.warn("REJECTED: {} | Raw line: {}", ex.getMessage(), line);
                }
            }
        } catch (Exception e) {
            log.error("Error while streaming CSV: {}", e.getMessage(), e);
        }

        log.info("================ Processing Complete ================");
        log.info("Total Rows Evaluated:  {}", totalRows);
        log.info("Valid Events Parsed:   {}", validEvents);
        log.info("Rejected Rows:         {}", rejectedRows);
        log.info("Successfully Sent:     {}", sentEvents);
    }
}
