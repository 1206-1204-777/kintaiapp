package com.example.kinntai.dto;

import java.time.LocalTime;

import com.example.kinntai.entity.UserRole;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private String location;
    private UserRole role;
    private String email;
    private LocalTime startTime;
    private LocalTime endTime;
}