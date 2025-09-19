package com.example.springproject.repository;



import com.example.springproject.entity.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Optional<Event> findById(Long id);
    List<Event> findAllOrderByDateAsc();
    List<Event> findByNameContaining(String name);
    boolean existsById(Long id);
    void save(Event event);
    void update(Event event);
    void deleteById(Long id);
}