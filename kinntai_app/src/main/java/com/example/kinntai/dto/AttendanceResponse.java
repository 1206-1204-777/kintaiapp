package com.example.kinntai.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AttendanceResponse {
    private Long id;
    private Long userId;
    private LocalDate date;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private boolean isWorking;
}