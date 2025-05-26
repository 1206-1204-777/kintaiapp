package com.example.kinntai.dto;

import lombok.Data;

@Data
public class LocationRequest {
    private String name;
    private String startTime;
    private String endTime;
    private String createdBy;
}