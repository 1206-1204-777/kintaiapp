package com.example.kinntai.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat; // JsonFormatをインポート

import lombok.Data;

@Data
public class PersonalHolidayResponse {
    private Long id;
    private Long userId;
    private String username;        // 申請者名
    
    @JsonFormat(pattern = "yyyy-MM-dd") // 日付フォーマットを指定
    private LocalDate holidayDate;

    private String holidayType;
    private String reason;
    private String status;          // 申請ステータス (PENDING, APPROVED, REJECTEDなど、文字列で送る)
    private String approverName;    // 承認者名
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // 日時フォーマットを指定
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // 日時フォーマットを指定
    private LocalDateTime updatedAt;
}
