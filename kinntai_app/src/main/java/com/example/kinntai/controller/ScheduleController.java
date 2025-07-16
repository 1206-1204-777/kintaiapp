package com.example.kinntai.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.config.ApiResponse;
import com.example.kinntai.dto.ScheduleRequestDto;
import com.example.kinntai.entity.Schedule;
import com.example.kinntai.service.ScheduleService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/schedule")
@AllArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 📌 スケジュール提出 (既存)
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse> submitSchedule(@RequestBody ScheduleRequestDto request) {
        scheduleService.saveSchedule(request); // saveSchedule を再利用
        return ResponseEntity.ok(new ApiResponse("スケジュールを提出しました",true));
    }

    // 📌 スケジュール保存 (新規追加) - saveScheduleData から呼ばれる
    // submit と同じ DTO を使うので、メソッド名を分けることで用途を明確にする
    @PostMapping("/save")
    public ResponseEntity<ApiResponse> saveSchedule(@RequestBody ScheduleRequestDto request) {
        scheduleService.saveSchedule(request); // saveSchedule を再利用
        return ResponseEntity.ok(new ApiResponse("スケジュールを保存しました",true));
    }

    // 📌 指定週のスケジュールを取得（ユーザー表示用）(既存)
    @GetMapping("/week")
    public ResponseEntity<List<Schedule>> getWeeklySchedule(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start) {
        List<Schedule> schedules = scheduleService.getWeeklySchedule(userId, start);
        return ResponseEntity.ok(schedules);
    }

    // 📌 管理者による承認（オプション）(既存)
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approveSchedule(@PathVariable Long id) {
        scheduleService.approveSchedule(id);
        return ResponseEntity.ok(new ApiResponse("スケジュールを承認しました",true));
    }

    // 📌 提出済みスケジュール履歴を取得 (新規追加) - loadSubmittedSchedules から呼ばれる。
    @GetMapping("/submitted")
    public ResponseEntity<List<Schedule>> getSubmittedSchedules(@RequestParam Long userId) {

        LocalDate now = LocalDate.now();
        List<Schedule> submittedSchedules = scheduleService.getWeeklySchedule(userId, now.withDayOfMonth(1)); // 仮の取得
        return ResponseEntity.ok(submittedSchedules); // 仮の戻り値
    }
}