//package com.example.springproject.service;
//
//
//import com.example.springproject.entity.Role;
//import com.example.springproject.mapper.UserMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class RoleService {
//    private final UserMapper userMapper;
//
//    public List<Role> getAllRoles() {
//        return userMapper.findAllRoles();
//    }
//
//    public void assignRoleToUser(Long userId, Integer roleId) {
//        if (userMapper.userHasRole(userId, roleId)) {
//            log.warn("User {} already has role {}", userId, roleId);
//            throw new IllegalArgumentException("User already has this role");
//        }
//
//        userMapper.addRoleToUser(userId, roleId);
//        log.info("Assigned role {} to user {}", roleId, userId);
//    }
//
//    public void removeRoleFromUser(Long userId, Integer roleId) {
//        if (!userMapper.userHasRole(userId, roleId)) {
//            log.warn("User {} doesn't have role {}", userId, roleId);
//            throw new IllegalArgumentException("User doesn't have this role");
//        }
//
//        userMapper.removeRoleFromUser(userId, roleId);
//        log.info("Removed role {} from user {}", roleId, userId);
//    }
//
//    public List<Role> getUserRoles(Long userId) {
//        return userMapper.findRolesByUserId(userId);
//    }
//
//    public boolean userHasRole(Long userId, String roleName) {
//        List<Role> userRoles = getUserRoles(userId);
//        return userRoles.stream()
//                .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
//    }
//}

package com.example.springproject.service;

import com.example.springproject.entity.Role;
import com.example.springproject.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
    private final UserMapper userMapper;

    @Cacheable(value = "roles:all")
    public List<Role> getAllRoles() {
        log.info("Fetching all roles from DB");
        return userMapper.findAllRoles();
    }

    @CacheEvict(value = "roles:user", key = "#userId") // clear user roles cache
    public void assignRoleToUser(Long userId, Integer roleId) {
        if (userMapper.userHasRole(userId, roleId)) {
            log.warn("User {} already has role {}", userId, roleId);
            throw new IllegalArgumentException("User already has this role");
        }
        userMapper.addRoleToUser(userId, roleId);
        log.info("Assigned role {} to user {}", roleId, userId);
    }

    @CacheEvict(value = "roles:user", key = "#userId")
    public void removeRoleFromUser(Long userId, Integer roleId) {
        if (!userMapper.userHasRole(userId, roleId)) {
            log.warn("User {} doesn't have role {}", userId, roleId);
            throw new IllegalArgumentException("User doesn't have this role");
        }
        userMapper.removeRoleFromUser(userId, roleId);
        log.info("Removed role {} from user {}", roleId, userId);
    }

    @Cacheable(value = "roles:user", key = "#userId")
    public List<Role> getUserRoles(Long userId) {
        log.info("Fetching roles for user {} from DB", userId);
        return userMapper.findRolesByUserId(userId);
    }

    // No cache here since it's just filtering the cached result
    public boolean userHasRole(Long userId, String roleName) {
        List<Role> userRoles = getUserRoles(userId); // will hit cache if available
        return userRoles.stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
    }
}
