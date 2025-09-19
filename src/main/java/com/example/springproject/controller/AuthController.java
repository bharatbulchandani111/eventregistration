package com.example.springproject.controller;


import com.example.springproject.dto.JwtResponse;
import com.example.springproject.dto.LoginRequest;
import com.example.springproject.dto.SignupRequest;
import com.example.springproject.entity.User;
import com.example.springproject.repository.UserRepository;
import com.example.springproject.service.AuthService;
import com.example.springproject.service.UserService;
import com.example.springproject.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());

        try {
            String jwt = authService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
            User user = (User) userService.loadUserByUsername(loginRequest.getUsername());

            String roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles));

        } catch (Exception e) {
            log.error("Login failed for username: {}", loginRequest.getUsername(), e);
            return ResponseEntity.badRequest().body("Error: Invalid credentials");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        log.info("Registration attempt for username: {}", signUpRequest.getUsername());

        try {
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                log.warn("Username {} already exists", signUpRequest.getUsername());
                return ResponseEntity.badRequest().body("Error: Username is already taken!");
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                log.warn("Email {} already exists", signUpRequest.getEmail());
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }

            // Create new user
            User user = User.builder()
                    .username(signUpRequest.getUsername())
                    .email(signUpRequest.getEmail())
                    .password(passwordEncoder.encode(signUpRequest.getPassword()))
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();

            userRepository.save(user);
            log.info("User {} registered successfully", signUpRequest.getUsername());

            return ResponseEntity.ok("User registered successfully!");

        } catch (Exception e) {
            log.error("Registration failed for username: {}", signUpRequest.getUsername(), e);
            return ResponseEntity.badRequest().body("Error: Registration failed");
        }
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        log.info("Checking username availability: {}", username);
        boolean exists = userRepository.existsByUsername(username);
        return ResponseEntity.ok().body(!exists);
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmailAvailability(@PathVariable String email) {
        log.info("Checking email availability: {}", email);
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok().body(!exists);
    }
}