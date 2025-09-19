package com.example.springproject.controller;




import com.example.springproject.entity.Event;
import com.example.springproject.entity.Registration;
import com.example.springproject.repository.EventRepository;
import com.example.springproject.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // In a real implementation, you'd get the user ID from the authenticated user
        // For this example, we'll assume the username is the user ID
        return 1L; // Replace with actual user ID retrieval logic
    }

    @GetMapping("/my-registrations")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Registration>> getMyRegistrations() {
        Long userId = getCurrentUserId();
        log.info("Fetching registrations for user id: {}", userId);

        try {
            List<Registration> registrations = registrationRepository.findByUserId(userId);
            log.info("Found {} registrations for user {}", registrations.size(), userId);
            return ResponseEntity.ok(registrations);
        } catch (Exception e) {
            log.error("Error fetching registrations for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> registerForEvent(@PathVariable Long eventId) {
        Long userId = getCurrentUserId();
        log.info("User {} registering for event {}", userId, eventId);

        try {
            // Check if event exists
            Optional<Event> event = eventRepository.findById(eventId);
            if (event.isEmpty()) {
                log.warn("Event not found with id: {}", eventId);
                return ResponseEntity.notFound().build();
            }

            // Check if user is already registered
            if (registrationRepository.existsByUserIdAndEventId(userId, eventId)) {
                log.warn("User {} already registered for event {}", userId, eventId);
                return ResponseEntity.badRequest().body("Already registered for this event");
            }

            // Check event capacity
            Integer currentRegistrations = registrationRepository.countByEventId(eventId);
            if (currentRegistrations >= event.get().getCapacity()) {
                log.warn("Event {} is at full capacity", eventId);
                return ResponseEntity.badRequest().body("Event is full");
            }

            // Create registration
            Registration registration = Registration.builder()
                    .userId(userId)
                    .eventId(eventId)
                    .registrationDate(LocalDateTime.now())
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();

            registrationRepository.save(registration);
            log.info("User {} successfully registered for event {}", userId, eventId);
            return ResponseEntity.ok(registration);

        } catch (Exception e) {
            log.error("Error registering for event: {}", eventId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/event/{eventId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelRegistration(@PathVariable Long eventId) {
        Long userId = getCurrentUserId();
        log.info("User {} cancelling registration for event {}", userId, eventId);

        try {
            Optional<Registration> registration = registrationRepository.findByUserIdAndEventId(userId, eventId);
            if (registration.isPresent()) {
                registrationRepository.deleteByUserIdAndEventId(userId, eventId);
                log.info("Registration cancelled for user {} and event {}", userId, eventId);
                return ResponseEntity.ok().build();
            } else {
                log.warn("Registration not found for user {} and event {}", userId, eventId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error cancelling registration for event: {}", eventId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<Integer> getEventRegistrationCount(@PathVariable Long eventId) {
        log.info("Getting registration count for event: {}", eventId);

        try {
            Integer count = registrationRepository.countByEventId(eventId);
            log.info("Event {} has {} registrations", eventId, count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting registration count for event: {}", eventId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/event/{eventId}/check")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> checkUserRegistration(@PathVariable Long eventId) {
        Long userId = getCurrentUserId();
        log.info("Checking if user {} is registered for event {}", userId, eventId);

        try {
            boolean isRegistered = registrationRepository.existsByUserIdAndEventId(userId, eventId);
            log.info("User {} registered for event {}: {}", userId, eventId, isRegistered);
            return ResponseEntity.ok(isRegistered);
        } catch (Exception e) {
            log.error("Error checking registration status for event: {}", eventId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}