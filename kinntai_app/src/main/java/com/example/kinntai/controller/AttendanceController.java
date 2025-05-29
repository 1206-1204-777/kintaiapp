package com.example.kinntai.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.AttendanceResponse;
import com.example.kinntai.entity.Attendance;
import com.example.kinntai.service.AttendanceService;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 出勤処理
     */
    @PostMapping("/clock-in/{userId}")
    public ResponseEntity<Attendance> clockIn(@PathVariable Long userId) {
        try {
            System.out.println("出勤リクエスト: userId=" + userId);
            Attendance attendance = attendanceService.clockIn(userId);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            System.err.println("出勤処理エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 退勤処理
     */
    @PostMapping("/clock-out/{userId}")
    public ResponseEntity<Attendance> clockOut(@PathVariable Long userId) {
        try {
            System.out.println("退勤リクエスト: userId=" + userId);
            Attendance attendance = attendanceService.clockOut(userId);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            System.err.println("退勤処理エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 勤務状態の取得
     */
    @GetMapping("/{userId}/status")
    public ResponseEntity<AttendanceResponse> getAttendanceStatus(@PathVariable Long userId) {
        try {
            System.out.println("勤務状態取得: userId=" + userId);
            AttendanceResponse status = attendanceService.getAttendanceStatus(userId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            System.err.println("勤務状態取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 特定日の勤怠情報取得
     */
    @GetMapping("/{userId}/date/{date}")
    public ResponseEntity<Attendance> getAttendanceByDate(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            System.out.println("日次勤怠情報取得: userId=" + userId + ", date=" + date);
            
            return attendanceService.getAttendanceByDate(userId, date)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("日次勤怠情報取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 月次勤怠情報取得
     */
    @GetMapping("/monthly/{userId}")
    public ResponseEntity<List<Attendance>> getMonthlyAttendance(
            @PathVariable Long userId,
            @RequestParam String month) {
        try {
            System.out.println("勤怠履歴取得: userId=" + userId + ", month=" + month);
            
            List<Attendance> attendances = attendanceService.getMonthlyAttendance(userId, month);
            
            return ResponseEntity.ok(attendances);
        } catch (Exception e) {
            System.err.println("月次勤怠情報取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 週次勤怠情報取得
     */
    @GetMapping("/weekly/{userId}")
    public ResponseEntity<List<Attendance>> getWeeklyAttendance(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            System.out.println("週次勤怠情報取得: userId=" + userId + ", startDate=" + startDate + ", endDate=" + endDate);
            
            List<Attendance> attendances = attendanceService.generateWeeklyAttendances(userId, startDate, endDate);
            
            return ResponseEntity.ok(attendances);
        } catch (Exception e) {
            System.err.println("週次勤怠情報取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}