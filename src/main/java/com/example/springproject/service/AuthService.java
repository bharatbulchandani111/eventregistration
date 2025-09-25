package com.example.springproject.service;

import com.example.springproject.dto.JwtResponse;
import com.example.springproject.entity.User;
import com.example.springproject.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

//    @Transactional(readOnly = true)
//    public String authenticateUser(String username, String password) {
//        log.info("Authenticating user: {}", username);
//
//        try {
//            Authentication authentication = authenticationManager.authenticateUser(
//                    new UsernamePasswordAuthenticationToken(username, password));
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            String jwt = jwtUtil.generateJwtToken(authentication);
//
//            log.info("User {} authenticated successfully with roles: {}", username,
//                    authentication.getAuthorities().stream()
//                            .map(GrantedAuthority::getAuthority)
//                            .collect(Collectors.joining(", ")));
//            return jwt;
//
//        } catch (Exception e) {
//            log.error("Authentication failed for user: {}", username, e);
//            throw new IllegalArgumentException("Invalid credentials");
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public User getCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            log.warn("No authenticated user found");
//            throw new IllegalStateException("No authenticated user");
//        }
//
//        String username = authentication.getName();
//        log.debug("Getting current user: {}", username);
//
//        return (User) userService.loadUserByUsername(username);
//    }


    @Transactional(readOnly = true)
    public JwtResponse authenticateUser(String username, String password) {
        log.info("Authenticating user: {}", username);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateJwtToken(authentication);

        User user = (User) authentication.getPrincipal();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        log.info("User {} authenticated successfully with roles: {}", username, roles);

        return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        String username = authentication.getName();
        log.debug("Retrieving current user: {}", username);

        return (User) userService.loadUserByUsername(username);
    }


    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user.getId();
    }

    @Transactional(readOnly = true)
    public String getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "";
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    @Transactional(readOnly = true)
    public boolean hasRole(String role) {
        String roles = getCurrentUserRoles();
        return roles.contains(role);
    }

    @Transactional(readOnly = true)
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    @Transactional(readOnly = true)
    public void validateUserAccess(Long userId) {
        Long currentUserId = getCurrentUserId();
        boolean isAdmin = isAdmin();

        if (!currentUserId.equals(userId) && !isAdmin) {
            log.warn("User {} attempted to access resources of user {}", currentUserId, userId);
            throw new SecurityException("Access denied");
        }
    }
}