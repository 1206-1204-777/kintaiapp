package com.example.kinntai.controller.admin;

import java.util.HashMap;
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

import com.example.kinntai.service.admin.AdminHolidayRequestService;
import com.example.kinntai.service.impl.EditRequestServiceImpl;
import com.example.kinntai.service.impl.OvertimeServiceImpl;
import com.example.kinntai.service.impl.ScheduleServiceImpl;

/**
 * 統合申請管理APIコントローラー
 * 休暇、勤怠修正、スケジュール、残業申請を統合管理
 */
@RestController
@RequestMapping("/api/admin/requests")
@CrossOrigin(origins = "*")
public class AdminUnifiedRequestController {

    @Autowired
    private AdminHolidayRequestService holidayRequestService;
    
    @Autowired
    private EditRequestServiceImpl editRequestService;
    
    @Autowired
    private ScheduleServiceImpl scheduleService;
    
    @Autowired
    private OvertimeServiceImpl overtimeService;

    /**
     * 全申請の統計情報を取得
     * 
     * @return 申請統計
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRequestStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 各申請タイプの統計を取得
            Map<String, Integer> byType = new HashMap<>();
            byType.put("holiday", getHolidayRequestCount());
            byType.put("edit", editRequestService.getTotalRequestCount());
            byType.put("schedule", scheduleService.getTotalRequestCount());
            byType.put("overtime", overtimeService.getTotalRequestCount());
            
            // 全体統計
            int total = byType.values().stream().mapToInt(Integer::intValue).sum();
            int pending = getPendingRequestCount();
            int approved = getApprovedRequestCount();
            int rejected = getRejectedRequestCount();
            
            stats.put("total", total);
            stats.put("pending", pending);
            stats.put("approved", approved);
            stats.put("rejected", rejected);
            stats.put("byType", byType);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("申請統計取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 勤怠修正申請の管理者用承認
     * 
     * @param requestId 申請ID
     * @param approverId 承認者ID
     * @return 処理結果
     */
    @PutMapping("/edit/{requestId}/approve")
    public ResponseEntity<?> approveEditRequest(
            @PathVariable Long requestId,
            @RequestParam Long approverId) {
        try {
            editRequestService.approveRequest(requestId, approverId);
            return ResponseEntity.ok().body("勤怠修正申請を承認しました");
        } catch (Exception e) {
            System.err.println("勤怠修正承認エラー: " + e.getMessage());
            return ResponseEntity.badRequest().body("承認に失敗しました: " + e.getMessage());
        }
    }

    /**
     * 勤怠修正申請の管理者用却下
     * 
     * @param requestId 申請ID
     * @param approverId 承認者ID
     * @return 処理結果
     */
    @PutMapping("/edit/{requestId}/reject")
    public ResponseEntity<?> rejectEditRequest(
            @PathVariable Long requestId,
            @RequestParam Long approverId) {
        try {
            editRequestService.rejectRequest(requestId, approverId);
            return ResponseEntity.ok().body("勤怠修正申請を却下しました");
        } catch (Exception e) {
            System.err.println("勤怠修正却下エラー: " + e.getMessage());
            return ResponseEntity.badRequest().body("却下に失敗しました: " + e.getMessage());
        }
    }

    /**
     * スケジュール申請の管理者用却下
     * 
     * @param requestId 申請ID
     * @return 処理結果
     */
    @PutMapping("/schedule/{requestId}/reject")
    public ResponseEntity<?> rejectScheduleRequest(@PathVariable Long requestId) {
        try {
            scheduleService.rejectSchedule(requestId);
            return ResponseEntity.ok().body("スケジュール申請を却下しました");
        } catch (Exception e) {
            System.err.println("スケジュール却下エラー: " + e.getMessage());
            return ResponseEntity.badRequest().body("却下に失敗しました: " + e.getMessage());
        }
    }

    // ===== プライベートヘルパーメソッド =====
    
    private int getHolidayRequestCount() {
        try {
            var requests = holidayRequestService.getAllHolidayRequests(null, null);
            return requests.size();
        } catch (Exception e) {
            return 0;
        }
    }

    private int getPendingRequestCount() {
        try {
            var holidayPending = holidayRequestService.getAllHolidayRequests("pending", null);
            int editPending = editRequestService.getPendingEditRequestCount();
            int schedulePending = scheduleService.getPendingScheduleRequestCount();
            int overtimePending = overtimeService.getPendingOvertimeRequestCount();
            
            return holidayPending.size() + editPending + schedulePending + overtimePending;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getApprovedRequestCount() {
        try {
            var holidayApproved = holidayRequestService.getAllHolidayRequests("approved", null);
            // 他の申請タイプの承認済み数も加算する場合は以下のコメントアウトを外す
            // int editApproved = editRequestService.getEditRequestsByStatus(RequestStatus.APPROVED).size();
            // int scheduleApproved = scheduleService.getScheduleRequestsByStatus(RequestStatus.APPROVED).size();
            // int overtimeApproved = overtimeService.getOvertimeRequestsByStatus(RequestStatus.APPROVED).size();
            
            return holidayApproved.size(); // 仮の実装
        } catch (Exception e) {
            return 0;
        }
    }

    private int getRejectedRequestCount() {
        try {
            var holidayRejected = holidayRequestService.getAllHolidayRequests("rejected", null);
            // 他の申請タイプの却下数も加算する場合は以下のコメントアウトを外す
            // int editRejected = editRequestService.getEditRequestsByStatus(RequestStatus.REJECTED).size();
            // int scheduleRejected = scheduleService.getScheduleRequestsByStatus(RequestStatus.REJECTED).size();
            // int overtimeRejected = overtimeService.getOvertimeRequestsByStatus(RequestStatus.REJECTED).size();
            
            return holidayRejected.size(); // 仮の実装
        } catch (Exception e) {
            return 0;
        }
    }
}