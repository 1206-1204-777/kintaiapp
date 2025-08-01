package com.example.kinntai.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 勤怠統計レスポンスDTO
 * ダッシュボード表示用の統計情報
 */
@Data // Getter, Setter, equals, hashCode, toString を自動生成
@NoArgsConstructor // 引数なしコンストラクタを自動生成
@AllArgsConstructor // 全フィールドを持つコンストラクタを自動生成
public class AttendanceStatsResponse {
    private int present;              // 出勤者数
    private int working;				// ★★★ 勤務中者数を追加 ★★★
    private int absent;               // 未出勤者数
    private int overtime;             // 残業者数
    private double totalOvertimeHours; // 総残業時間
    private int totalEmployees;       // 総社員数
}
