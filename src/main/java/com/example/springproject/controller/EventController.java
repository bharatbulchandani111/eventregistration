package com.example.springproject.controller;

import com.example.springproject.dto.EventRequest;
import com.example.springproject.entity.Event;
import com.example.springproject.repository.EventRepository;
import com.example.springproject.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventRepository eventRepository;
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        log.info("Fetching all events");
        try {
            List<Event> events = eventRepository.findAllOrderByDateAsc();
            log.info("Found {} events", events.size());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error fetching events", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        log.info("Fetching event with id: {}", id);
        try {
            Optional<Event> event = eventRepository.findById(id);
            if (event.isPresent()) {
                log.info("Event found: {}", event.get().getName());
                return ResponseEntity.ok(event.get());
            } else {
                log.warn("Event not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error fetching event with id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String name) {
        log.info("Searching events with name containing: {}", name);
        try {
            List<Event> events = eventRepository.findByNameContaining(name);
            log.info("Found {} events matching search", events.size());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error searching events", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        log.info("Creating new event: {}", eventRequest.getName());
        try {
            Event event = eventService.createEvent(
                    eventRequest.getName(),
                    eventRequest.getDescription(),
                    eventRequest.getDate(),
                    eventRequest.getLocation(),
                    eventRequest.getCapacity()
            );
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest eventRequest) {
        log.info("Updating event with id: {}", id);
        try {
            Optional<Event> existingEvent = eventRepository.findById(id);
            if (existingEvent.isPresent()) {
                Event event = existingEvent.get();
                event.setName(eventRequest.getName());
                event.setDescription(eventRequest.getDescription());
                event.setDate(eventRequest.getDate());
                event.setLocation(eventRequest.getLocation());
                event.setCapacity(eventRequest.getCapacity());
                event.setUpdatedOn(LocalDateTime.now());

                eventRepository.update(event);
                log.info("Event updated successfully: {}", event.getName());
                return ResponseEntity.ok(event);
            } else {
                log.warn("Event not found for update with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating event with id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        log.info("Deleting event with id: {}", id);
        try {
            if (eventRepository.existsById(id)) {
                eventRepository.deleteById(id);
                log.info("Event deleted successfully with id: {}", id);
                return ResponseEntity.ok().build();
            } else {
                log.warn("Event not found for deletion with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting event with id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}