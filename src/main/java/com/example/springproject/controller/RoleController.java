package com.example.springproject.controller;

import com.example.springproject.entity.Role;
import com.example.springproject.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            log.error("Failed to fetch roles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch roles"));
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<?> getUserRoles(@PathVariable Long userId) {
        try {
            List<Role> roles = roleService.getUserRoles(userId);
            if (roles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No roles found for user " + userId));
            }
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            log.error("Failed to fetch roles for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch user roles"));
        }
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRoleToUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Integer roleId = Integer.valueOf(request.get("roleId").toString());

            roleService.assignRoleToUser(userId, roleId);
            return ResponseEntity.ok(Map.of("message", "Role assigned successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Role assignment failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while assigning role", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to assign role"));
        }
    }

    @PostMapping("/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeRoleFromUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Integer roleId = Integer.valueOf(request.get("roleId").toString());

            roleService.removeRoleFromUser(userId, roleId);
            return ResponseEntity.ok(Map.of("message", "Role removed successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Role removal failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while removing role", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to remove role"));
        }
    }

    @PostMapping("/user/{userId}/promote-to-admin")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long userId) {
        try {
            roleService.assignRoleToUser(userId, 4); // 2 = ADMIN role ID
            return ResponseEntity.ok(Map.of("message", "User promoted to admin successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Promote to admin failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "User is already an admin"));
        } catch (Exception e) {
            log.error("Unexpected error during promotion", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to promote user to admin"));
        }
    }

    @PostMapping("/user/{userId}/demote-to-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> demoteToUser(@PathVariable Long userId) {
        try {
            roleService.removeRoleFromUser(userId, 2); // 2 = ADMIN role ID
            return ResponseEntity.ok(Map.of("message", "User demoted to regular user"));
        } catch (IllegalArgumentException e) {
            log.warn("Demote to user failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User is not an admin"));
        } catch (Exception e) {
            log.error("Unexpected error during demotion", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to demote user"));
        }
    }
}
