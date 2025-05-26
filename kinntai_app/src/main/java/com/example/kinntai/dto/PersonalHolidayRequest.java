package com.example.kinntai.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PersonalHolidayRequest {
    private Long userId;        // 申請者のID
    private LocalDate holidayDate;  // 休日日付
    private String holidayType; // 休日種別 (PAID, SPECIAL, SICK, OTHERなど、文字列で受け取る)
    private String reason;      // 理由 (任意)
}