package com.example.springproject.service;

import com.example.springproject.entity.Event;
import com.example.springproject.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        log.debug("Getting all events");
        List<Event> events = eventRepository.findAllOrderByDateAsc();
        log.info("Retrieved {} events", events.size());
        return events;
    }

    @Transactional(readOnly = true)
    public Optional<Event> getEventById(Long id) {
        log.debug("Getting event by ID: {}", id);
        Optional<Event> event = eventRepository.findById(id);
        event.ifPresentOrElse(
                e -> log.debug("Event found: {}", e.getName()),
                () -> log.warn("Event not found with ID: {}", id)
        );
        return event;
    }

    @Transactional(readOnly = true)
    public List<Event> searchEvents(String name) {
        log.debug("Searching events with name containing: {}", name);
        List<Event> events = eventRepository.findByNameContaining(name);
        log.info("Found {} events matching search: {}", events.size(), name);
        return events;
    }

    @Transactional(readOnly = true)
    public boolean eventExists(Long id) {
        log.debug("Checking if event exists with ID: {}", id);
        return eventRepository.existsById(id);
    }

    @Transactional
    public Event createEvent(String name, String description, LocalDateTime date,
                             String location, Integer capacity) {
        log.info("Creating new event: {}", name);

        if (capacity != null && capacity < 1) {
            log.warn("Invalid capacity for event: {}", capacity);
            throw new IllegalArgumentException("Event capacity must be at least 1");
        }

        if (date != null && date.isBefore(LocalDateTime.now())) {
            log.warn("Event date is in the past: {}", date);
            throw new IllegalArgumentException("Event date cannot be in the past");
        }

        Event event = Event.builder()
                .name(name)
                .description(description)
                .date(date)
                .location(location)
                .capacity(capacity)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        eventRepository.save(event);
        log.info("Event created successfully with ID: {}", event.getId());
        return event;
    }

    @Transactional
    public Event updateEvent(Long id, String name, String description, LocalDateTime date,
                             String location, Integer capacity) {
        log.info("Updating event with ID: {}", id);

        Optional<Event> existingEvent = eventRepository.findById(id);
        if (existingEvent.isEmpty()) {
            log.warn("Event not found for update with ID: {}", id);
            throw new IllegalArgumentException("Event not found with ID: " + id);
        }

        if (capacity != null && capacity < 1) {
            log.warn("Invalid capacity for event: {}", capacity);
            throw new IllegalArgumentException("Event capacity must be at least 1");
        }

        if (date != null && date.isBefore(LocalDateTime.now())) {
            log.warn("Event date is in the past: {}", date);
            throw new IllegalArgumentException("Event date cannot be in the past");
        }

        Event event = existingEvent.get();
        event.setName(name);
        event.setDescription(description);
        event.setDate(date);
        event.setLocation(location);
        event.setCapacity(capacity);
        event.setUpdatedOn(LocalDateTime.now());

        eventRepository.update(event);
        log.info("Event updated successfully: {}", event.getName());
        return event;
    }

    @Transactional
    public void deleteEvent(Long id) {
        log.info("Deleting event with ID: {}", id);

        if (!eventRepository.existsById(id)) {
            log.warn("Event not found for deletion with ID: {}", id);
            throw new IllegalArgumentException("Event not found with ID: " + id);
        }

        eventRepository.deleteById(id);
        log.info("Event deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean isEventFull(Long eventId) {
        log.debug("Checking if event is full: {}", eventId);
        // This would be implemented with registration service
        // For now, we'll assume it's not full
        return false;
    }

    @Transactional(readOnly = true)
    public boolean isEventActive(Long eventId) {
        log.debug("Checking if event is active: {}", eventId);
        Optional<Event> event = eventRepository.findById(eventId);
        return event.map(e -> e.getDate() == null || e.getDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }
}