package com.example.springproject.controller;

import com.example.springproject.entity.Registration;
import com.example.springproject.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final RegistrationService registrationService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // TODO: Replace with actual user ID retrieval from your UserDetails/Principal
        return 4L;
    }

    @GetMapping("/my-registrations")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyRegistrations() {
        Long userId = getCurrentUserId();
        try {
            List<Registration> registrations = registrationService.getUserRegistrations(userId);
            if (registrations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No registrations found for user " + userId));
            }
            return ResponseEntity.ok(registrations);
        } catch (Exception e) {
            log.error("Failed to fetch registrations for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch registrations"));
        }
    }

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> registerForEvent(@PathVariable Long eventId) {
        Long userId = getCurrentUserId();
        try {
            Registration registration = registrationService.registerForEvent(userId, eventId);
            return ResponseEntity.status(HttpStatus.CREATED).body(registration);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for user {} and event {}: {}", userId, eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during registration for event {}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to register for event"));
        }
    }

    @DeleteMapping("/event/{eventId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelRegistration(@PathVariable Long eventId) {
        Long userId = getCurrentUserId();
        try {
            registrationService.cancelRegistration(userId, eventId);
            return ResponseEntity.ok(Map.of("message", "Registration cancelled successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Cancellation failed for user {} and event {}: {}", userId, eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during cancellation for event {}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cancel registration"));
        }
    }

    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<?> getEventRegistrationCount(@PathVariable Long eventId) {
        try {
            int count = registrationService.getEventRegistrationCount(eventId);
            return ResponseEntity.ok(Map.of("eventId", eventId, "count", count));
        } catch (Exception e) {
            log.error("Failed to get registration count for event {}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get registration count"));
        }
    }

    @GetMapping("/event/{eventId}/check")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> checkUserRegistration(@PathVariable Long eventId) {
        Long userId = getCurrentUserId();
        try {
            boolean registered = registrationService.isUserRegistered(userId, eventId);
            return ResponseEntity.ok(Map.of("userId", userId, "eventId", eventId, "registered", registered));
        } catch (Exception e) {
            log.error("Failed to check registration for user {} and event {}", userId, eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check registration"));
        }
    }
}
