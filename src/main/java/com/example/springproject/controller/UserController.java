package com.example.springproject.controller;


import com.example.springproject.entity.User;
import com.example.springproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users (admin only)");
        // In a real implementation, you would have a service method to get all users
        // For this example, we'll return an empty list as we don't have that method
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                log.info("User found: {}", user.get().getUsername());
                return ResponseEntity.ok(user.get());
            } else {
                log.warn("User not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error fetching user with id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<User> getCurrentUserProfile() {
        log.info("Fetching current user profile");
        // In a real implementation, you'd get the current user from security context
        // For this example, we'll return a mock response
        try {
            // This would be the actual implementation:
            // String username = SecurityContextHolder.getContext().getAuthentication().getName();
            // Optional<User> user = userRepository.findByUsername(username);

            Optional<User> user = userRepository.findById(1L); // Mock
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error fetching user profile", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        try {
            if (userRepository.findById(id).isPresent()) {
                userRepository.deleteById(id);
                log.info("User deleted successfully with id: {}", id);
                return ResponseEntity.ok().build();
            } else {
                log.warn("User not found for deletion with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}