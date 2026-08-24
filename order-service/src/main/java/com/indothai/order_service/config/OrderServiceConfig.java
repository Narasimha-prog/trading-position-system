package com.indothai.order_service.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "order")
public class OrderServiceConfig {

    private String csvPath = "classpath:order_updates.csv";
    private String targetUrl = "http://localhost:8080";
    private int rateLimit = 50;

}
