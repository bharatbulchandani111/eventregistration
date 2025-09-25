package com.example.springproject.controller;

import com.example.springproject.dto.JwtResponse;
import com.example.springproject.dto.LoginRequest;
import com.example.springproject.dto.SignupRequest;
import com.example.springproject.entity.User;
import com.example.springproject.service.AuthService;
import com.example.springproject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());

        try {
            JwtResponse jwt = authService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
            User user = (User) userService.loadUserByUsername(loginRequest.getUsername());

            String roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            return ResponseEntity.ok(jwt);

        } catch (Exception e) {
            log.error("Login failed for username: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        log.info("Registration attempt for username: {}", signUpRequest.getUsername());

        try {
            User user = userService.registerNewUser(signUpRequest);
            log.info("User {} registered successfully", user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User registered successfully"));

        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error during registration for username: {}", signUpRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed due to server error"));
        }
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        log.info("Checking username availability: {}", username);
        boolean available = !userService.usernameExists(username);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmailAvailability(@PathVariable String email) {
        log.info("Checking email availability: {}", email);
        boolean available = !userService.emailExists(email);
        return ResponseEntity.ok(Map.of("available", available));
    }
}
