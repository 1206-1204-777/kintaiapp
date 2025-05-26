package com.example.kinntai.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CompanyHolidayResponse {
    private Long id;
    private LocalDate holidayDate;
    private String holidayName;
    private String createdByUsername;   // 登録者名
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}