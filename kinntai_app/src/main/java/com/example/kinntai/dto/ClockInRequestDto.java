package com.example.kinntai.dto;

import lombok.Data;

@Data
public class ClockInRequestDto {
    private Long userId;
    private String type; // "WORK" or "REMOTE"
}
