package com.example.kinntai.dto;

import java.time.LocalTime;

import lombok.Data;

@Data
public class UserLocationResponse {
    private Long userId;
    private String username;
    private Long locationId;
    private String locationName;
    private LocalTime startTime;
    private LocalTime endTime;
}