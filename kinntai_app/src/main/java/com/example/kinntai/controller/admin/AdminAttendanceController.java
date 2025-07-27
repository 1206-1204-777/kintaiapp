package com.example.kinntai.controller.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.admin.AdminAttendanceUpdateRequest;
import com.example.kinntai.dto.admin.AttendanceStatsResponse;
import com.example.kinntai.dto.admin.TodayAttendanceResponse;
import com.example.kinntai.service.admin.AdminAttendanceService;

/**
 * 管理者向け勤怠管理APIコントローラー
 * 全社員の勤怠状況の確認、統計情報の取得、勤怠データの編集機能を提供
 */
@RestController
@RequestMapping("/api/admin/attendance")
@CrossOrigin(origins = "*")
public class AdminAttendanceController {

    @Autowired
    private AdminAttendanceService adminAttendanceService;

    /**
     * ダッシュボード用の勤怠統計データを取得
     * 今日の出勤状況、残業時間、未出勤者数などの統計情報
     *
     * @return 勤怠統計データ
     */
    @GetMapping("/stats")
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
     * 今日の全社員勤怠状況を取得
     * 各社員の出勤時刻、退勤時刻、勤務状況を一覧で取得
     *
     * @return 今日の勤怠状況一覧
     */
    @GetMapping("/today")
    public ResponseEntity<List<TodayAttendanceResponse>> getTodayAttendance() {
        try {
            List<TodayAttendanceResponse> attendanceList = adminAttendanceService.getTodayAttendanceList();
            return ResponseEntity.ok(attendanceList);
        } catch (Exception e) {
            System.err.println("今日の勤怠状況取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 指定した日付の全社員勤怠状況を取得
     *
     * @param date 対象日付 (YYYY-MM-DD形式)
     * @return 指定日の勤怠状況一覧
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<TodayAttendanceResponse>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<TodayAttendanceResponse> attendanceList = adminAttendanceService.getAttendanceListByDate(date);
            return ResponseEntity.ok(attendanceList);
        } catch (Exception e) {
            System.err.println("指定日勤怠状況取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 管理者による勤怠データの編集
     * 社員の出勤・退勤時刻を管理者権限で修正
     *
     * @param attendanceId 勤怠記録ID
     * @param request 修正内容
     * @return 修正結果
     */
    @PutMapping("/{attendanceId}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long attendanceId,
            @RequestBody AdminAttendanceUpdateRequest request) {
        try {
            adminAttendanceService.updateAttendanceByAdmin(attendanceId, request);
            return ResponseEntity.ok().body("勤怠データを更新しました");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("勤怠データ更新エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("勤怠データの更新に失敗しました");
        }
    }

    /**
     * 期間指定での勤怠データ取得
     * 管理者がレポート作成や分析のために使用
     *
     * @param startDate 開始日
     * @param endDate 終了日
     * @param userId ユーザーID (オプション、指定時は特定ユーザーのみ)
     * @return 期間内の勤怠データ
     */
    @GetMapping("/period")
    public ResponseEntity<List<TodayAttendanceResponse>> getAttendanceByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long userId) {
        try {
            List<TodayAttendanceResponse> attendanceList;
            if (userId != null) {
                attendanceList = adminAttendanceService.getAttendanceByPeriodAndUser(startDate, endDate, userId);
            } else {
                attendanceList = adminAttendanceService.getAttendanceByPeriod(startDate, endDate, userId);
            }
            return ResponseEntity.ok(attendanceList);
        } catch (Exception e) {
            System.err.println("期間指定勤怠データ取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 未退勤者の一覧を取得
     * 管理者が退勤漏れをチェックするために使用
     *
     * @param date 対象日付 (省略時は今日)
     * @return 未退勤者一覧
     */
    @GetMapping("/unclocked-out")
    public ResponseEntity<List<TodayAttendanceResponse>> getUnclockedOutUsers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            LocalDate targetDate = (date != null) ? date : LocalDate.now();
            List<TodayAttendanceResponse> unclockedUsers = adminAttendanceService.getUnclockedOutUsers(targetDate);
            return ResponseEntity.ok(unclockedUsers);
        } catch (Exception e) {
            System.err.println("未退勤者取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}