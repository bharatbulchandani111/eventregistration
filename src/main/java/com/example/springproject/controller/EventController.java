package com.example.springproject.controller;

import com.example.springproject.dto.EventRequest;
import com.example.springproject.entity.Event;
import com.example.springproject.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            if (events.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No events found"));
            }
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch all events", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch events"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        try {
            Event event = eventService.getEventById(id);
            if (event == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Event not found with id " + id));
            }
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            log.error("Failed to fetch event {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch event"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchEvents(@RequestParam String name) {
        try {
            List<Event> events = eventService.searchEvents(name);
            if (events.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No events found with name: " + name));
            }
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to search events with name {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search events"));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventRequest request) {
        try {
            Event event = Event.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .date(request.getDate())
                    .location(request.getLocation())
                    .capacity(request.getCapacity())
                    .build();

            Event created = eventService.createEvent(event);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (Exception e) {
            log.error("Failed to create event {}", request.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create event"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id,
                                         @Valid @RequestBody EventRequest request) {
        try {
            Event event = Event.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .date(request.getDate())
                    .location(request.getLocation())
                    .capacity(request.getCapacity())
                    .build();

            Event updated = eventService.updateEvent(id, event);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Event not found with id " + id));
            }
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            log.error("Failed to update event {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update event"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Delete failed, event {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to delete event {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete event"));
        }
    }
}
