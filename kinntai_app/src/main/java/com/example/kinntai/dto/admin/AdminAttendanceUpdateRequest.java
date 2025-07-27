// AdminAttendanceUpdateRequest.java
package com.example.kinntai.dto.admin;

import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理者による勤怠データ更新リクエストDTO
 */
@Data
@NoArgsConstructor
public class AdminAttendanceUpdateRequest {
    private LocalTime clockInTime;    // 出勤時刻
    private LocalTime clockOutTime;   // 退勤時刻
    private String reason;            // 修正理由
}