package com.example.kinntai.controller.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.admin.AttendanceStatsResponse;
import com.example.kinntai.service.admin.AdminAttendanceService;
import com.example.kinntai.service.admin.AdminDashboardService;
import com.example.kinntai.service.admin.AdminHolidayRequestService;

/**
 * 管理者ダッシュボードAPIコントローラー
 * ダッシュボード表示に必要な統計情報や概要データを提供
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private AdminAttendanceService adminAttendanceService;

    @Autowired
    private AdminHolidayRequestService adminHolidayRequestService;

    /**
     * ダッシュボード用の統合統計データを取得
     * 勤怠統計、休暇申請統計、アラート情報などを一括取得
     *
     * @return ダッシュボード統計データ
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> dashboardStats = adminDashboardService.getDashboardStats();
            return ResponseEntity.ok(dashboardStats);
        } catch (Exception e) {
            System.err.println("ダッシュボード統計取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 勤怠統計データのみを取得
     * 勤怠関連の統計情報のみが必要な場合に使用
     *
     * @return 勤怠統計データ
     */
    @GetMapping("/attendance-stats")
    public ResponseEntity<AttendanceStatsResponse> getAttendanceStats() {
        try {
            AttendanceStatsResponse stats = adminAttendanceService.getTodayAttendanceStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("勤怠統計取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 休暇申請統計データのみを取得
     * 休暇申請関連の統計情報のみが必要な場合に使用
     *
     * @return 休暇申請統計データ
     */
    @GetMapping("/holiday-stats")
    public ResponseEntity<Map<String, Object>> getHolidayStats() {
        try {
            Map<String, Object> stats = adminHolidayRequestService.getHolidayRequestStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("休暇申請統計取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * システムアラート情報を取得
     * 未退勤者、承認待ち申請数などの注意喚起情報
     *
     * @return アラート情報
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getSystemAlerts() {
        try {
            Map<String, Object> alerts = adminDashboardService.getSystemAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            System.err.println("システムアラート取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 月次サマリー統計を取得
     * 過去数ヶ月の勤怠トレンドデータ
     *
     * @return 月次サマリー統計
     */
    @GetMapping("/monthly-summary")
    public ResponseEntity<Map<String, Object>> getMonthlySummary() {
        try {
            Map<String, Object> summary = adminDashboardService.getMonthlySummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            System.err.println("月次サマリー取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 部署別統計を取得
     * 各部署の勤怠状況比較データ
     *
     * @return 部署別統計データ
     */
    @GetMapping("/department-stats")
    public ResponseEntity<Map<String, Object>> getDepartmentStats() {
        try {
            Map<String, Object> stats = adminDashboardService.getDepartmentStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("部署別統計取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}