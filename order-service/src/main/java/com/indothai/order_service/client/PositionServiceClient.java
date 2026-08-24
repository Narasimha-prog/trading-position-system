package com.indothai.order_service.client;
import com.indothai.order_service.parser.OrderCsvParser.ParsedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Slf4j
public class PositionServiceClient {


    private final RestClient restClient;

    public PositionServiceClient() {
        this.restClient = RestClient.create();
    }

    public boolean sendEvent(String targetUrl, ParsedEvent event) {
        try {
            Map<String, Object> payload = Map.of(
                    "event_id", event.eventId(),
                    "symbol", event.symbol(),
                    "transaction_type", event.transactionType(),
                    "quantity", event.quantity()
            );

            var response = restClient.post()
                    .uri(targetUrl + "/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SENT: {} [{} {} {}] -> {}",
                        event.eventId(), event.symbol(), event.transactionType(), event.quantity(), response.getBody());
                return true;
            } else {
                log.error("FAILED delivery for {}: HTTP {}", event.eventId(), response.getStatusCode());
                return false;
            }
        } catch (Exception ex) {
            log.error("CONNECTION ERROR delivering event {} to {}: {}", event.eventId(), targetUrl, ex.getMessage());
            return false;
        }
    }
}
