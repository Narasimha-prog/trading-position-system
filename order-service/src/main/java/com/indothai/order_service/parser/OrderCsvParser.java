package com.indothai.order_service.parser;


import java.util.Optional;

public class OrderCsvParser {

    public record ParsedEvent(String eventId, String symbol, String transactionType, int quantity) {}

    public static Optional<ParsedEvent> parseAndValidateRow(String line, long rowNum) throws IllegalArgumentException {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Row " + rowNum + ": Empty line");
        }

        String[] tokens = line.split(",", -1);
        if (tokens.length != 4) {
            throw new IllegalArgumentException("Row " + rowNum + ": Expected 4 columns, found " + tokens.length);
        }

        String eventId = tokens[0].trim();
        if (eventId.isEmpty()) {
            throw new IllegalArgumentException("Row " + rowNum + ": 'event_id' is blank");
        }

        String symbol = tokens[1].trim();
        if (symbol.isEmpty()) {
            throw new IllegalArgumentException("Row " + rowNum + ": 'symbol' is blank");
        }

        String txType = tokens[2].trim();
        if (!"BUY".equals(txType) && !"SELL".equals(txType)) {
            throw new IllegalArgumentException("Row " + rowNum + ": Invalid transaction_type '" + txType + "' (must be BUY or SELL)");
        }

        String qtyStr = tokens[3].trim();
        int quantity;
        try {
            quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) {
                throw new IllegalArgumentException("Row " + rowNum + ": Quantity must be a positive integer (got " + quantity + ")");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Row " + rowNum + ": Invalid non-integer quantity '" + qtyStr + "'");
        }

        return Optional.of(new ParsedEvent(eventId, symbol, txType, quantity));
    }
}
