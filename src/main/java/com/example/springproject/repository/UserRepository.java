package com.example.springproject.repository;



import com.example.springproject.entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void save(User user);
    void update(User user);
    void deleteById(Long id);
}