package com.example.springproject.service;

import com.example.springproject.entity.Event;
import com.example.springproject.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "events:all")
    public List<Event> getAllEvents() {
        log.info("Fetching all events from DB");
        return eventRepository.findAllOrderByDateAsc();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "events", key = "#id")
    public Event getEventById(Long id) {
        log.info("Fetching event {} from DB", id);
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "events:search", key = "#name")
    public List<Event> searchEvents(String name) {
        log.info("Searching events by name: {}", name);
        return eventRepository.findByNameContaining(name);
    }

    @Transactional
    @CacheEvict(value = {"events:all", "events:search"}, allEntries = true)
    public Event createEvent(Event event) {
        validateEvent(event);
        event.setCreatedOn(LocalDateTime.now());
        event.setUpdatedOn(LocalDateTime.now());
        eventRepository.save(event);
        log.info("Created event {}", event.getName());
        return event;
    }

    @Transactional
    @CachePut(value = "events", key = "#id")
    @CacheEvict(value = {"events:all", "events:search"}, allEntries = true)
    public Event updateEvent(Long id, Event updatedEvent) {
        Event existingEvent = getEventById(id);
        validateEvent(updatedEvent);

        existingEvent.setName(updatedEvent.getName());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setDate(updatedEvent.getDate());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setCapacity(updatedEvent.getCapacity());
        existingEvent.setUpdatedOn(LocalDateTime.now());

        eventRepository.update(existingEvent);
        log.info("Updated event {}", id);
        return existingEvent;
    }

    @Transactional
    @CacheEvict(value = {"events", "events:all", "events:search"}, key = "#id", allEntries = true)
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new IllegalArgumentException("Event not found with ID: " + id);
        }
        eventRepository.deleteById(id);
        log.info("Deleted event {}", id);
    }

    private void validateEvent(Event event) {
        if (event.getCapacity() != null && event.getCapacity() < 1) {
            throw new IllegalArgumentException("Event capacity must be at least 1");
        }
        if (event.getDate() != null && event.getDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past");
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "events:active", key = "#eventId")
    public boolean isEventActive(Long eventId) {
        log.debug("Checking if event {} is active", eventId);
        return eventRepository.findById(eventId)
                .map(event -> event.getDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }
}
