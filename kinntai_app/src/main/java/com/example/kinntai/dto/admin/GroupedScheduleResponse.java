// GroupedScheduleResponse.java
package com.example.kinntai.dto.admin;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * グループ化されたスケジュール申請レスポンスDTO
 * ユーザー別・月別にグループ化したスケジュール情報
 */
@Data
@NoArgsConstructor
public class GroupedScheduleResponse {
    private Long userId;                    // ユーザーID
    private String userName;                // ユーザー名
    private String targetMonth;             // 対象月 (YYYY-MM)
    private String status;                  // 全体ステータス
    private String requestDate;             // 最初の申請日
    private Integer totalDays;              // 総日数
    private Integer workDays;               // 出勤日数
    private Integer holidayDays;            // 休暇日数
    private Integer remoteDays;             // リモート日数
    private List<ScheduleDetail> scheduleDetails; // 詳細スケジュール
    
    @Data
    @NoArgsConstructor
    public static class ScheduleDetail {
        private Long id;                    // スケジュールID
        private String date;                // 日付
        private String type;                // 勤務タイプ
        private String status;              // 個別ステータス
    }
}