package com.example.kinntai.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String token;  // JWTトークンなどの認証トークン
    private Long locationId;
    private LocalTime startTime;
    private LocalTime endTime;
    private String locationName;
}