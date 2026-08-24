package com.indothai.position_service.service;


import com.indothai.position_service.dto.OrderEventDto;
import com.indothai.position_service.types.TransactionType;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PositionTrackerService {

    // Thread-safe concurrent structures to handle simultaneous reads and writes
    private final Map<String, Long> positions = new ConcurrentHashMap<>();
    private final Set<String> seenEventIds = ConcurrentHashMap.newKeySet();


    public boolean processEvent(OrderEventDto event) {

        if (!seenEventIds.add(event.eventId())) {
            return false;
        }

        long delta = (event.transactionType() == TransactionType.BUY)
                ? event.quantity()
                : -((long) event.quantity());


        positions.compute(event.symbol(), (sym, currentBalance) ->
                (currentBalance == null ? 0L : currentBalance) + delta
        );

        return true;
    }

    public Map<String, Long> getPositions() {
        return Collections.unmodifiableMap(positions);
    }

    public void clear() {
        positions.clear();
        seenEventIds.clear();
    }
}