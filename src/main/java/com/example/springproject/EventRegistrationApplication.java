package com.example.springproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example.springproject",
        "com.example.springproject.controller",
        "com.example.springproject.service",
        "com.example.springproject.config",
        "com.example.springproject.util",
        "com.example.springproject.dto"
})
@EntityScan("com.example.springproject.entity")  // Fixed package
@EnableJpaRepositories("com.example.springproject.repository")  // Fixed package
@EnableCaching
public class EventRegistrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventRegistrationApplication.class, args);
    }

}