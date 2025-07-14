package com.example.kinntai.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor 
public class EditRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDate targetDate;

    private LocalTime currentClockIn;
    private LocalTime requestedClockIn;

    private LocalTime currentClockOut;
    private LocalTime requestedClockOut;

    private String reason;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private Long approverId;

    private LocalDateTime requestDate;
    private LocalDateTime approvedDate;
}
