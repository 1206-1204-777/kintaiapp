package com.example.kinntai.dto;

import java.time.LocalTime;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private String location;
    private String email;
    private LocalTime startTime;
    private LocalTime endTime;
}