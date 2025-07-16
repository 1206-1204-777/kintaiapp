package com.example.kinntai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SubmittedScheduleResponseDto {
    private String id; // フロントエンドのSubmittedScheduleのidがstringなので合わせる
    private String month; // "YYYY-MM"形式
    private String submittedAt; // 提出日時 (ISO 8601形式の文字列)
    private String status; // "submitted", "approved", "rejected"
    private String approverName; // 承認者名
    private int workDays; // 出勤日数
    private int holidayDays; // 休日日数
    private Long userId; // ユーザーID
}
