package com.example.springproject.entity;



import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Registration extends BaseEntity {
    private Long id;
    private Long userId;
    private Long eventId;
    private LocalDateTime registrationDate;
}