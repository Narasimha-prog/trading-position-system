package com.indothai.order_service.parser;



import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderCsvParserTest {

    @Test
    @DisplayName("Parses valid CSV row correctly")
    void testValidRow() {
        var parsed = OrderCsvParser.parseAndValidateRow("evt-0001,RELIANCE,BUY,90", 2).orElseThrow();
        assertThat(parsed.eventId()).isEqualTo("evt-0001");
        assertThat(parsed.symbol()).isEqualTo("RELIANCE");
        assertThat(parsed.transactionType()).isEqualTo("BUY");
        assertThat(parsed.quantity()).isEqualTo(90);
    }

    @ParameterizedTest
    @ValueSource(strings = {"evt-1,TCS,HOLD,50", "evt-1,TCS,buy,50", "evt-1,TCS,INVALID,50"})
    @DisplayName("Rejects invalid transaction types")
    void testInvalidTransactionType(String row) {
        assertThatThrownBy(() -> OrderCsvParser.parseAndValidateRow(row, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("transaction_type");
    }

    @ParameterizedTest
    @ValueSource(strings = {"evt-1,TCS,BUY,0", "evt-1,TCS,BUY,-10", "evt-1,TCS,BUY,abc", "evt-1,TCS,BUY,"})
    @DisplayName("Rejects invalid quantities (zero, negative, non-integer, blank)")
    void testInvalidQuantity(String row) {
        assertThatThrownBy(() -> OrderCsvParser.parseAndValidateRow(row, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(ex -> assertThat(ex.getMessage().toLowerCase()).contains("quantity"));
    }

    @ParameterizedTest
    @ValueSource(strings = {",TCS,BUY,50", "   ,TCS,BUY,50", "evt-1,,BUY,50", "evt-1,   ,BUY,50"})
    @DisplayName("Rejects blank event IDs and blank symbols")
    void testBlankIdentifiers(String row) {
        assertThatThrownBy(() -> OrderCsvParser.parseAndValidateRow(row, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }
}