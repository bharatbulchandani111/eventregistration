package com.example.springproject.repository;


import com.example.springproject.entity.Registration;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository {
    Optional<Registration> findById(Long id);
    List<Registration> findByUserId(Long userId);
    List<Registration> findByEventId(Long eventId);
    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);
    Integer countByEventId(Long eventId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    void save(Registration registration);
    void deleteById(Long id);
    void deleteByUserIdAndEventId(Long userId, Long eventId);
}