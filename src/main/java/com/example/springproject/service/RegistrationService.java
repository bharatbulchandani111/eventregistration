//package com.example.springproject.service;
//
//import com.example.springproject.entity.Event;
//import com.example.springproject.entity.Registration;
//import com.example.springproject.repository.EventRepository;
//import com.example.springproject.repository.RegistrationRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class RegistrationService {
//    private final RegistrationRepository registrationRepository;
//    private final EventRepository eventRepository;
//    private final EventService eventService;
//
//    @Transactional(readOnly = true)
//    public List<Registration> getUserRegistrations(Long userId) {
//        return registrationRepository.findByUserId(userId);
//    }
//
//    @Transactional(readOnly = true)
//    public List<Registration> getEventRegistrations(Long eventId) {
//        return registrationRepository.findByEventId(eventId);
//    }
//
//    @Transactional(readOnly = true)
//    public Optional<Registration> getRegistration(Long userId, Long eventId) {
//        return registrationRepository.findByUserIdAndEventId(userId, eventId);
//    }
//
//    @Transactional(readOnly = true)
//    public Integer getEventRegistrationCount(Long eventId) {
//        return registrationRepository.countByEventId(eventId);
//    }
//
//    @Transactional(readOnly = true)
//    public boolean isUserRegistered(Long userId, Long eventId) {
//        return registrationRepository.existsByUserIdAndEventId(userId, eventId);
//    }
//
//    @Transactional
//    public Registration registerForEvent(Long userId, Long eventId) {
//        Event event = eventRepository.findById(eventId)
//                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
//
//        if (isUserRegistered(userId, eventId)) {
//            throw new IllegalArgumentException("User already registered for this event");
//        }
//
//        if (event.getCapacity() != null && getEventRegistrationCount(eventId) >= event.getCapacity()) {
//            throw new IllegalArgumentException("Event is full");
//        }
//
//        if (!eventService.isEventActive(eventId)) {
//            throw new IllegalArgumentException("Event is not active");
//        }
//
//        Registration registration = Registration.builder()
//                .userId(userId)
//                .eventId(eventId)
//                .registrationDate(LocalDateTime.now())
//                .createdOn(LocalDateTime.now())
//                .updatedOn(LocalDateTime.now())
//                .build();
//
//        registrationRepository.save(registration);
//        return registration;
//    }
//
//    @Transactional
//    public void cancelRegistration(Long userId, Long eventId) {
//        if (registrationRepository.findByUserIdAndEventId(userId, eventId).isEmpty()) {
//            throw new IllegalArgumentException("Registration not found");
//        }
//        registrationRepository.deleteByUserIdAndEventId(userId, eventId);
//    }
//
//    @Transactional
//    public void cancelRegistrationById(Long registrationId) {
//        if (registrationRepository.findById(registrationId).isEmpty()) {
//            throw new IllegalArgumentException("Registration not found with ID: " + registrationId);
//        }
//        registrationRepository.deleteById(registrationId);
//    }
//
//    @Transactional(readOnly = true)
//    public boolean canUserRegister(Long userId, Long eventId) {
//        Optional<Event> event = eventRepository.findById(eventId);
//        if (event.isEmpty() || !eventService.isEventActive(eventId)) {
//            return false;
//        }
//        if (isUserRegistered(userId, eventId)) {
//            return false;
//        }
//        if (event.get().getCapacity() != null &&
//                getEventRegistrationCount(eventId) >= event.get().getCapacity()) {
//            return false;
//        }
//        return true;
//    }
//}


package com.example.springproject.service;

import com.example.springproject.entity.Event;
import com.example.springproject.entity.Registration;
import com.example.springproject.repository.EventRepository;
import com.example.springproject.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Transactional(readOnly = true)
    @Cacheable(value = "userRegistrations", key = "#userId")
    public List<Registration> getUserRegistrations(Long userId) {
        log.debug("Fetching registrations from database for user: {}", userId);
        return registrationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "eventRegistrations", key = "#eventId")
    public List<Registration> getEventRegistrations(Long eventId) {
        log.debug("Fetching registrations from database for event: {}", eventId);
        return registrationRepository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userEventRegistration", key = "#userId + '-' + #eventId")
    public Optional<Registration> getRegistration(Long userId, Long eventId) {
        log.debug("Fetching registration from database for user: {} and event: {}", userId, eventId);
        return registrationRepository.findByUserIdAndEventId(userId, eventId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "eventRegistrationCount", key = "#eventId")
    public Integer getEventRegistrationCount(Long eventId) {
        log.debug("Fetching registration count from database for event: {}", eventId);
        return registrationRepository.countByEventId(eventId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userRegisteredCheck", key = "#userId + '-' + #eventId")
    public boolean isUserRegistered(Long userId, Long eventId) {
        log.debug("Checking registration status from database for user: {} and event: {}", userId, eventId);
        return registrationRepository.existsByUserIdAndEventId(userId, eventId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userRegistrations", key = "#userId"),
            @CacheEvict(value = "eventRegistrations", key = "#eventId"),
            @CacheEvict(value = "eventRegistrationCount", key = "#eventId"),
            @CacheEvict(value = "userEventRegistration", key = "#userId + '-' + #eventId"),
            @CacheEvict(value = "userRegisteredCheck", key = "#userId + '-' + #eventId")
    })
    public Registration registerForEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        if (isUserRegisteredDirectly(userId, eventId)) {
            throw new IllegalArgumentException("User already registered for this event");
        }

        if (event.getCapacity() != null && getEventRegistrationCountDirectly(eventId) >= event.getCapacity()) {
            throw new IllegalArgumentException("Event is full");
        }

        if (!eventService.isEventActive(eventId)) {
            throw new IllegalArgumentException("Event is not active");
        }

        Registration registration = Registration.builder()
                .userId(userId)
                .eventId(eventId)
                .registrationDate(LocalDateTime.now())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        registrationRepository.save(registration);
        log.info("User {} successfully registered for event {}", userId, eventId);
        return registration;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userRegistrations", key = "#userId"),
            @CacheEvict(value = "eventRegistrations", key = "#eventId"),
            @CacheEvict(value = "eventRegistrationCount", key = "#eventId"),
            @CacheEvict(value = "userEventRegistration", key = "#userId + '-' + #eventId"),
            @CacheEvict(value = "userRegisteredCheck", key = "#userId + '-' + #eventId")
    })
    public void cancelRegistration(Long userId, Long eventId) {
        if (registrationRepository.findByUserIdAndEventId(userId, eventId).isEmpty()) {
            throw new IllegalArgumentException("Registration not found");
        }
        registrationRepository.deleteByUserIdAndEventId(userId, eventId);
        log.info("Registration cancelled for user {} and event {}", userId, eventId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userRegistrations", allEntries = true),
            @CacheEvict(value = "eventRegistrations", allEntries = true),
            @CacheEvict(value = "eventRegistrationCount", allEntries = true),
            @CacheEvict(value = "userEventRegistration", allEntries = true),
            @CacheEvict(value = "userRegisteredCheck", allEntries = true)
    })
    public void cancelRegistrationById(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with ID: " + registrationId));

        registrationRepository.deleteById(registrationId);
        log.info("Registration with ID {} cancelled", registrationId);
    }

    @Transactional(readOnly = true)
    public boolean canUserRegister(Long userId, Long eventId) {
        // This method should NOT be cached as it needs real-time data
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty() || !eventService.isEventActive(eventId)) {
            return false;
        }
        if (isUserRegisteredDirectly(userId, eventId)) {
            return false;
        }
        if (event.get().getCapacity() != null &&
                getEventRegistrationCountDirectly(eventId) >= event.get().getCapacity()) {
            return false;
        }
        return true;
    }

    // Helper methods to bypass cache when needed
    private boolean isUserRegisteredDirectly(Long userId, Long eventId) {
        return registrationRepository.existsByUserIdAndEventId(userId, eventId);
    }

    private Integer getEventRegistrationCountDirectly(Long eventId) {
        return registrationRepository.countByEventId(eventId);
    }
}