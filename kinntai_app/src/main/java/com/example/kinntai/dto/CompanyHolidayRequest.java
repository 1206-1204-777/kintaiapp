package com.example.kinntai.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CompanyHolidayRequest {
    private LocalDate holidayDate;      // 休日日付
    private String holidayName;         // 休日名
    private Long createdByUserId;       // 登録者のID (管理者など)
}