// HolidayRequestActionRequest.java
package com.example.kinntai.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 休暇申請の承認・却下アクションリクエストDTO
 */
@Data
@NoArgsConstructor
public class HolidayRequestActionRequest {
    private Long approverId;          // 承認者ID
    private String comment;           // コメント（承認時は任意、却下時は理由）
}