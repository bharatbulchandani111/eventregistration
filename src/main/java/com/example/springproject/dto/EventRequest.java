package com.example.springproject.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {
    private String name;
    private String description;
    private LocalDateTime date;
    private String location;
    private Integer capacity;
}