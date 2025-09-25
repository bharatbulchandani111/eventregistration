package com.example.springproject.service;

import com.example.springproject.entity.Event;
import com.example.springproject.entity.Registration;
import com.example.springproject.repository.EventRepository;
import com.example.springproject.repository.RegistrationRepository;
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
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Transactional(readOnly = true)
    public List<Registration> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Registration> getEventRegistrations(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public Optional<Registration> getRegistration(Long userId, Long eventId) {
        return registrationRepository.findByUserIdAndEventId(userId, eventId);
    }

    @Transactional(readOnly = true)
    public Integer getEventRegistrationCount(Long eventId) {
        return registrationRepository.countByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public boolean isUserRegistered(Long userId, Long eventId) {
        return registrationRepository.existsByUserIdAndEventId(userId, eventId);
    }

    @Transactional
    public Registration registerForEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        if (isUserRegistered(userId, eventId)) {
            throw new IllegalArgumentException("User already registered for this event");
        }

        if (event.getCapacity() != null && getEventRegistrationCount(eventId) >= event.getCapacity()) {
            throw new IllegalArgumentException("Event is full");
        }

        if (!eventService.isEventActive(eventId)) {
            throw new IllegalArgumentException("Event is not active");
        }

        Registration registration = Registration.builder()
                .userId(userId)
                .eventId(eventId)
                .registrationDate(LocalDateTime.now())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        registrationRepository.save(registration);
        return registration;
    }

    @Transactional
    public void cancelRegistration(Long userId, Long eventId) {
        if (registrationRepository.findByUserIdAndEventId(userId, eventId).isEmpty()) {
            throw new IllegalArgumentException("Registration not found");
        }
        registrationRepository.deleteByUserIdAndEventId(userId, eventId);
    }

    @Transactional
    public void cancelRegistrationById(Long registrationId) {
        if (registrationRepository.findById(registrationId).isEmpty()) {
            throw new IllegalArgumentException("Registration not found with ID: " + registrationId);
        }
        registrationRepository.deleteById(registrationId);
    }

    @Transactional(readOnly = true)
    public boolean canUserRegister(Long userId, Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty() || !eventService.isEventActive(eventId)) {
            return false;
        }
        if (isUserRegistered(userId, eventId)) {
            return false;
        }
        if (event.get().getCapacity() != null &&
                getEventRegistrationCount(eventId) >= event.get().getCapacity()) {
            return false;
        }
        return true;
    }
}
