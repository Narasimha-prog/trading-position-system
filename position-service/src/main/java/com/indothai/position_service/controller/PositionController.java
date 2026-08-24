package com.indothai.position_service.controller;


import com.indothai.position_service.dto.OrderEventDto;
import com.indothai.position_service.service.PositionTrackerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class PositionController {

    private static final Logger log = LoggerFactory.getLogger(PositionController.class);
    private final PositionTrackerService trackerService;

    public PositionController(PositionTrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @PostMapping("/events")
    public ResponseEntity<Map<String, String>> receiveEvent(@Valid @RequestBody OrderEventDto event) {
        boolean accepted = trackerService.processEvent(event);
        if (accepted) {
            log.info("ACCEPTED event: {} | Symbol: {} | Type: {} | Qty: {}",
                    event.eventId(), event.symbol(), event.transactionType(), event.quantity());
            return ResponseEntity.ok(Map.of("status", "ACCEPTED", "event_id", event.eventId()));
        } else {
            log.info("IGNORED duplicate event: {}", event.eventId());
            return ResponseEntity.ok(Map.of("status", "DUPLICATE_IGNORED", "event_id", event.eventId()));
        }
    }

    @GetMapping("/position")
    public ResponseEntity<Map<String, Long>> getPositions() {
        return ResponseEntity.ok(trackerService.getPositions());
    }
}
