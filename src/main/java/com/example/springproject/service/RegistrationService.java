package com.example.springproject.service;


import com.example.springproject.entity.Event;
import com.example.springproject.entity.Registration;
import com.example.springproject.repository.EventRepository;
import com.example.springproject.repository.RegistrationRepository;
import com.example.springproject.entity.Event;
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
        log.debug("Getting registrations for user ID: {}", userId);
        List<Registration> registrations = registrationRepository.findByUserId(userId);
        log.info("Retrieved {} registrations for user {}", registrations.size(), userId);
        return registrations;
    }

    @Transactional(readOnly = true)
    public List<Registration> getEventRegistrations(Long eventId) {
        log.debug("Getting registrations for event ID: {}", eventId);
        List<Registration> registrations = registrationRepository.findByEventId(eventId);
        log.info("Retrieved {} registrations for event {}", registrations.size(), eventId);
        return registrations;
    }

    @Transactional(readOnly = true)
    public Optional<Registration> getRegistration(Long userId, Long eventId) {
        log.debug("Getting registration for user {} and event {}", userId, eventId);
        Optional<Registration> registration = registrationRepository.findByUserIdAndEventId(userId, eventId);
        registration.ifPresentOrElse(
                r -> log.debug("Registration found"),
                () -> log.debug("Registration not found for user {} and event {}", userId, eventId)
        );
        return registration;
    }

    @Transactional(readOnly = true)
    public Integer getEventRegistrationCount(Long eventId) {
        log.debug("Getting registration count for event: {}", eventId);
        Integer count = registrationRepository.countByEventId(eventId);
        log.info("Event {} has {} registrations", eventId, count);
        return count;
    }

    @Transactional(readOnly = true)
    public boolean isUserRegistered(Long userId, Long eventId) {
        log.debug("Checking if user {} is registered for event {}", userId, eventId);
        boolean isRegistered = registrationRepository.existsByUserIdAndEventId(userId, eventId);
        log.debug("User {} registered for event {}: {}", userId, eventId, isRegistered);
        return isRegistered;
    }

    @Transactional
    public Registration registerForEvent(Long userId, Long eventId) {
        log.info("Registering user {} for event {}", userId, eventId);

        // Check if event exists

        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            log.warn("Event not found with ID: {}", eventId);
            throw new IllegalArgumentException("Event not found with ID: " + eventId);
        }

        // Check if user is already registered
        if (isUserRegistered(userId, eventId)) {
            log.warn("User {} already registered for event {}", userId, eventId);
            throw new IllegalArgumentException("User already registered for this event");
        }

        // Check event capacity
        Integer currentRegistrations = getEventRegistrationCount(eventId);
        if (event.get().getCapacity() != null && currentRegistrations >= event.get().getCapacity()) {
            log.warn("Event {} is at full capacity", eventId);
            throw new IllegalArgumentException("Event is full");
        }

        // Check if event is active
        if (!eventService.isEventActive(eventId)) {
            log.warn("Event {} is not active", eventId);
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
        log.info("User {} successfully registered for event {}", userId, eventId);
        return registration;
    }

    @Transactional
    public void cancelRegistration(Long userId, Long eventId) {
        log.info("Cancelling registration for user {} and event {}", userId, eventId);

        Optional<Registration> registration = registrationRepository.findByUserIdAndEventId(userId, eventId);
        if (registration.isEmpty()) {
            log.warn("Registration not found for user {} and event {}", userId, eventId);
            throw new IllegalArgumentException("Registration not found");
        }

        registrationRepository.deleteByUserIdAndEventId(userId, eventId);
        log.info("Registration cancelled for user {} and event {}", userId, eventId);
    }

    @Transactional
    public void cancelRegistrationById(Long registrationId) {
        log.info("Cancelling registration with ID: {}", registrationId);

        Optional<Registration> registration = registrationRepository.findById(registrationId);
        if (registration.isEmpty()) {
            log.warn("Registration not found with ID: {}", registrationId);
            throw new IllegalArgumentException("Registration not found with ID: " + registrationId);
        }

        registrationRepository.deleteById(registrationId);
        log.info("Registration cancelled with ID: {}", registrationId);
    }

    @Transactional(readOnly = true)
    public boolean canUserRegister(Long userId, Long eventId) {
        log.debug("Checking if user {} can register for event {}", userId, eventId);

        // Check if event exists and is active
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty() || !eventService.isEventActive(eventId)) {
            log.debug("Event {} not found or not active", eventId);
            return false;
        }

        // Check if user is already registered
        if (isUserRegistered(userId, eventId)) {
            log.debug("User {} already registered for event {}", userId, eventId);
            return false;
        }

        // Check event capacity
        Integer currentRegistrations = getEventRegistrationCount(eventId);
        if (event.get().getCapacity() != null && currentRegistrations >= event.get().getCapacity()) {
            log.debug("Event {} is at full capacity", eventId);
            return false;
        }

        log.debug("User {} can register for event {}", userId, eventId);
        return true;
    }
}