package com.example.kinntai.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PersonalHolidayResponse {
    private Long id;
    private Long userId;
    private String username;        // 申請者名
    private LocalDate holidayDate;
    private String holidayType;
    private String reason;
    private String status;          // 申請ステータス (PENDING, APPROVED, REJECTEDなど、文字列で送る)
    private String approverName;    // 承認者名
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}