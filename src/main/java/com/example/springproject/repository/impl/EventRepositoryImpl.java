package com.example.springproject.repository.impl;

import com.example.springproject.entity.Event;
import com.example.springproject.mapper.EventMapper;
import com.example.springproject.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {
    private final EventMapper eventMapper;

    @Override
    public Optional<Event> findById(Long id) {
        return eventMapper.findById(id);
    }

    @Override
    public List<Event> findAllOrderByDateAsc() {
        return eventMapper.findAllOrderByDateAsc();
    }

    @Override
    public List<Event> findByNameContaining(String name) {
        return eventMapper.findByNameContaining(name);
    }

    @Override
    public boolean existsById(Long id) {
        return eventMapper.existsById(id);
    }

    @Override
    public void save(Event event) {
        eventMapper.save(event);
    }

    @Override
    public void update(Event event) {
        eventMapper.update(event);
    }

    @Override
    public void deleteById(Long id) {
        eventMapper.deleteById(id);
    }
}