// AdminHolidayRequestResponse.java
package com.example.kinntai.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理者向け休暇申請レスポンスDTO
 */
@Data
@NoArgsConstructor
public class AdminHolidayRequestResponse {
    private Long id;                  // 申請ID
    private Long userId;              // 申請者ID
    private String userName;          // 申請者名
    private String startDate;         // 開始日
    private String endDate;           // 終了日
    private String reason;            // 申請理由
    private String status;            // 申請ステータス (pending/approved/rejected)
    private String requestDate;       // 申請日
    private String type;              // 休暇種別 (vacation/sick)
    private String approverName;      // 承認者名
}