package com.example.springproject.repository.impl;




import com.example.springproject.entity.Registration;
import com.example.springproject.mapper.RegistrationMapper;
import com.example.springproject.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegistrationRepositoryImpl implements RegistrationRepository {
    private final RegistrationMapper registrationMapper;

    @Override
    public Optional<Registration> findById(Long id) {
        return registrationMapper.findById(id);
    }

    @Override
    public List<Registration> findByUserId(Long userId) {
        return registrationMapper.findByUserId(userId);
    }

    @Override
    public List<Registration> findByEventId(Long eventId) {
        return registrationMapper.findByEventId(eventId);
    }

    @Override
    public Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId) {
        return registrationMapper.findByUserIdAndEventId(userId, eventId);
    }

    @Override
    public Integer countByEventId(Long eventId) {
        return registrationMapper.countByEventId(eventId);
    }

    @Override
    public boolean existsByUserIdAndEventId(Long userId, Long eventId) {
        return registrationMapper.existsByUserIdAndEventId(userId, eventId);
    }

    @Override
    public void save(Registration registration) {
        registrationMapper.save(registration);
    }

    @Override
    public void deleteById(Long id) {
        registrationMapper.deleteById(id);
    }

    @Override
    public void deleteByUserIdAndEventId(Long userId, Long eventId) {
        registrationMapper.deleteByUserIdAndEventId(userId, eventId);
    }
}