// UnifiedRequestResponse.java
package com.example.kinntai.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 統合申請レスポンスDTO
 * 休暇・スケジュール・勤怠修正申請を統合表示用
 */
@Data
@NoArgsConstructor
public class UnifiedRequestResponse {
    private Long id;                    // 申請ID
    private String type;                // 申請タイプ: "holiday", "schedule", "edit"
    private Long userId;                // 申請者ID
    private String userName;            // 申請者名
    private String requestDate;         // 申請日
    private String targetDate;          // 対象日（スケジュール・勤怠修正用）
    private String startDate;           // 開始日（休暇用）
    private String endDate;             // 終了日（休暇用）
    private String reason;              // 申請理由
    private String status;              // ステータス: "pending", "approved", "rejected"
    private String subType;             // サブタイプ: "vacation", "sick", "WORK", "HOLIDAY", "REMOTE"
    private String details;             // 詳細情報（勤怠修正の時刻変更など）
    private String approverName;        // 承認者名
    private String approvedDate;        // 承認日
    
    // 勤怠修正用の詳細フィールド
    private String currentClockIn;      // 現在の出勤時刻
    private String requestedClockIn;    // 申請出勤時刻
    private String currentClockOut;     // 現在の退勤時刻
    private String requestedClockOut;   // 申請退勤時刻
}