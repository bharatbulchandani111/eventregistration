package com.example.springproject.service;

import com.example.springproject.entity.User;
import com.example.springproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            log.warn("User not found with username: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        User userEntity = user.get();
        log.info("User loaded successfully: {} with {} roles", username, userEntity.getAuthorities().size());
        return userEntity;
    }


    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        log.debug("Getting user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        log.debug("Checking if username exists: {}", username);
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        log.debug("Checking if email exists: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User createUser(String username, String email, String password) {
        log.info("Creating new user: {}", username);

        if (usernameExists(username)) {
            log.warn("Username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        if (emailExists(email)) {
            log.warn("Email already exists: {}", email);
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        userRepository.save(user);
        log.info("User created successfully with ID: {}", user.getId());
        return user;
    }

    @Transactional
    public User updateUser(Long id, String username, String email) {
        log.info("Updating user with ID: {}", id);

        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            log.warn("User not found for update with ID: {}", id);
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        User user = existingUser.get();

        // Check if new username is taken by another user
        if (!user.getUsername().equals(username) && usernameExists(username)) {
            log.warn("Username already taken: {}", username);
            throw new IllegalArgumentException("Username already taken: " + username);
        }

        // Check if new email is taken by another user
        if (!user.getEmail().equals(email) && emailExists(email)) {
            log.warn("Email already taken: {}", email);
            throw new IllegalArgumentException("Email already taken: " + email);
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setUpdatedOn(LocalDateTime.now());

        userRepository.update(user);
        log.info("User updated successfully: {}", user.getUsername());
        return user;
    }

    @Transactional
    public void updateUserPassword(Long id, String newPassword) {
        log.info("Updating password for user ID: {}", id);

        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            log.warn("User not found for password update with ID: {}", id);
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        User user = existingUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedOn(LocalDateTime.now());

        userRepository.update(user);
        log.info("Password updated successfully for user: {}", user.getUsername());
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.findById(id).isPresent()) {
            log.warn("User not found for deletion with ID: {}", id);
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean validateUserCredentials(String username, String password) {
        log.debug("Validating credentials for user: {}", username);

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            log.warn("User not found for credential validation: {}", username);
            return false;
        }

        boolean isValid = passwordEncoder.matches(password, user.get().getPassword());
        log.debug("Credential validation result for {}: {}", username, isValid);
        return isValid;
    }
}