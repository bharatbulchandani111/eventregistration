package com.example.springproject.service;

import com.example.springproject.entity.Event;
import com.example.springproject.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<Event> getAllEvents() {
        return eventRepository.findAllOrderByDateAsc();
    }

    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Event> searchEvents(String name) {
        return eventRepository.findByNameContaining(name);
    }

    @Transactional
    public Event createEvent(Event event) {
        validateEvent(event);
        event.setCreatedOn(LocalDateTime.now());
        event.setUpdatedOn(LocalDateTime.now());
        eventRepository.save(event);
        return event;
    }

    @Transactional
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
        return existingEvent;
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new IllegalArgumentException("Event not found with ID: " + id);
        }
        eventRepository.deleteById(id);
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
    public boolean isEventActive(Long eventId) {
        return eventRepository.findById(eventId)
                .map(event -> {
                    boolean active = event.getDate().isAfter(LocalDateTime.now());
                    log.debug("Event {} active: {}", eventId, active);
                    return active;
                })
                .orElse(false);
    }
}
