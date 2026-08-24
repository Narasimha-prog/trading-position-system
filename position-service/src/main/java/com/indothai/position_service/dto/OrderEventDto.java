package com.indothai.position_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indothai.position_service.types.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderEventDto(
        @NotBlank(message = "event_id must not be blank")
        @JsonProperty("event_id")
        String eventId,

        @NotBlank(message = "symbol must not be blank")
        @JsonProperty("symbol")
        String symbol,

        @NotNull(message = "transaction_type must be BUY or SELL")
        @JsonProperty("transaction_type")
        TransactionType transactionType,

        @NotNull(message = "quantity must be provided")
        @Positive(message = "quantity must be a positive integer")
        @JsonProperty("quantity")
        Integer quantity
) {
}
