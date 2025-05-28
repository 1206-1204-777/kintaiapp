package com.example.kinntai.dto;

import java.time.LocalTime;

import com.example.kinntai.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private UserRole role;
    private String email;
    private String token;  // JWTトークンなどの認証トークン
    private Long locationId;
    private LocalTime startTime;
    private LocalTime endTime;
    private String locationName;
}