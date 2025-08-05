// AdminScheduleController.java
package com.example.kinntai.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.admin.GroupedScheduleResponse;
import com.example.kinntai.service.admin.AdminScheduleService;

/**
 * 管理者向けスケジュール管理APIコントローラー
 * グループ化されたスケジュール申請の管理
 */
@RestController
@RequestMapping("/api/admin/schedules")
@CrossOrigin(origins = "*")
public class AdminScheduleController {

    @Autowired
    private AdminScheduleService adminScheduleService;

    /**
     * グループ化されたスケジュール申請一覧を取得
     * 
     * @param status フィルター用ステータス
     * @return グループ化されたスケジュール一覧
     */
    @GetMapping("/grouped")
    public ResponseEntity<List<GroupedScheduleResponse>> getGroupedScheduleRequests(
            @RequestParam(required = false) String status) {
        try {
            List<GroupedScheduleResponse> schedules = adminScheduleService.getGroupedScheduleRequests(status);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            System.err.println("グループ化スケジュール取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 承認待ちのグループ化スケジュール申請を取得
     */
    @GetMapping("/pending")
    public ResponseEntity<List<GroupedScheduleResponse>> getPendingGroupedSchedules() {
        try {
            List<GroupedScheduleResponse> schedules = adminScheduleService.getPendingGroupedSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            System.err.println("承認待ちスケジュール取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ユーザーの月間スケジュールを一括承認
     * 
     * @param userId ユーザーID
     * @param targetMonth 対象月 (YYYY-MM)
     * @param approverId 承認者ID
     */
    @PutMapping("/approve/{userId}/{targetMonth}")
    public ResponseEntity<?> approveUserMonthlySchedule(
            @PathVariable Long userId,
            @PathVariable String targetMonth,
            @RequestParam Long approverId) {
        try {
            adminScheduleService.approveUserMonthlySchedule(userId, targetMonth, approverId);
            return ResponseEntity.ok().body("月間スケジュールを承認しました");
        } catch (Exception e) {
            System.err.println("スケジュール承認エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("承認に失敗しました: " + e.getMessage());
        }
    }

    /**
     * ユーザーの月間スケジュールを一括却下
     * 
     * @param userId ユーザーID
     * @param targetMonth 対象月 (YYYY-MM)
     * @param approverId 承認者ID
     */
    @PutMapping("/reject/{userId}/{targetMonth}")
    public ResponseEntity<?> rejectUserMonthlySchedule(
            @PathVariable Long userId,
            @PathVariable String targetMonth,
            @RequestParam Long approverId) {
        try {
            adminScheduleService.rejectUserMonthlySchedule(userId, targetMonth, approverId);
            return ResponseEntity.ok().body("月間スケジュールを却下しました");
        } catch (Exception e) {
            System.err.println("スケジュール却下エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("却下に失敗しました: " + e.getMessage());
        }
    }

    /**
     * スケジュール統計情報を取得
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getScheduleStatistics() {
        try {
            Map<String, Object> stats = adminScheduleService.getScheduleStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("スケジュール統計取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}