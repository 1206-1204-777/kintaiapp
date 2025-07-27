// TodayAttendanceResponse.java
package com.example.kinntai.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日の勤怠状況レスポンスDTO
 * 管理者画面での社員一覧表示用
 */
@Data
@NoArgsConstructor
public class TodayAttendanceResponse {
    private Long id;                  // ユーザーID
    private Long attendanceId;        // 勤怠記録ID
    private String name;              // 社員名
    private String email;             // メールアドレス
    private String department;        // 部署名
    private String avatar;            // アバター（イニシャル）
    private String workStartTime;     // 定時開始時刻
    private String workEndTime;       // 定時終了時刻
    private String todayClockIn;      // 今日の出勤時刻
    private String todayClockOut;     // 今日の退勤時刻
    private String status;            // 勤務状況 (present/absent/overtime)
    private Double overtimeHours;     // 残業時間
    private String overtimeType;      // 残業タイプ (early/late)
    private String phone;             // 電話番号
    private String joinDate;          // 入社日
    private String location;          // 勤務地
}