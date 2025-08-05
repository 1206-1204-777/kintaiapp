// AdminUnifiedRequestService.java
package com.example.kinntai.service.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.kinntai.dto.admin.UnifiedRequestResponse;
import com.example.kinntai.entity.EditRequest;
import com.example.kinntai.entity.PersonalHoliday;
import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.entity.Schedule;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.EditRequestRepository;
import com.example.kinntai.repository.PersonalHolidayRepository;
import com.example.kinntai.repository.ScheduleRepository;
import com.example.kinntai.repository.UserRepository;

/**
 * 統合申請管理サービス
 * 休暇・スケジュール・勤怠修正申請を統合管理
 */
@Service
public class AdminUnifiedRequestService {

    @Autowired
    private PersonalHolidayRepository personalHolidayRepository;
    
    @Autowired
    private EditRequestRepository editRequestRepository;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 全申請の統合一覧を取得
     * 
     * @param status フィルター用ステータス（null: 全て）
     * @param type フィルター用タイプ（null: 全て）
     * @return 統合申請一覧
     */
    public List<UnifiedRequestResponse> getAllUnifiedRequests(String status, String type) {
        List<UnifiedRequestResponse> allRequests = new ArrayList<>();
        
        // 休暇申請を取得
        if (type == null || "holiday".equals(type)) {
            List<PersonalHoliday> holidays = personalHolidayRepository.findAll();
            allRequests.addAll(holidays.stream()
                .filter(h -> status == null || h.getStatus().name().toLowerCase().equals(status))
                .map(this::convertHolidayToUnified)
                .collect(Collectors.toList()));
        }
        
        // 勤怠修正申請を取得
        if (type == null || "edit".equals(type)) {
            List<EditRequest> edits = editRequestRepository.findAll();
            allRequests.addAll(edits.stream()
                .filter(e -> status == null || e.getStatus().name().toLowerCase().equals(status))
                .map(this::convertEditToUnified)
                .collect(Collectors.toList()));
        }
        
        // スケジュール申請を取得
        if (type == null || "schedule".equals(type)) {
            List<Schedule> schedules = scheduleRepository.findAll();
            allRequests.addAll(schedules.stream()
                .filter(s -> status == null || s.getStatus().name().toLowerCase().equals(status))
                .map(this::convertScheduleToUnified)
                .collect(Collectors.toList()));
        }
        
        // 申請日順（新しい順）でソート
        return allRequests.stream()
            .sorted((a, b) -> b.getRequestDate().compareTo(a.getRequestDate()))
            .collect(Collectors.toList());
    }

    /**
     * 承認待ち申請のみを取得
     */
    public List<UnifiedRequestResponse> getPendingRequests() {
        return getAllUnifiedRequests("pending", null);
    }

    /**
     * 申請統計情報を取得
     */
    public Map<String, Object> getRequestStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 各申請タイプの統計
        long holidayPending = personalHolidayRepository.findAll().stream()
            .filter(h -> h.getStatus() == RequestStatus.PENDING).count();
        
        long editPending = editRequestRepository.findAll().stream()
            .filter(e -> e.getStatus() == RequestStatus.PENDING).count();
            
        long schedulePending = scheduleRepository.findAll().stream()
            .filter(s -> s.getStatus() == RequestStatus.PENDING).count();
        
        // 統計データを格納
        stats.put("totalPending", holidayPending + editPending + schedulePending);
        stats.put("holidayPending", holidayPending);
        stats.put("editPending", editPending);
        stats.put("schedulePending", schedulePending);
        
        Map<String, Long> byType = new HashMap<>();
        byType.put("holiday", personalHolidayRepository.count());
        byType.put("edit", editRequestRepository.count());
        byType.put("schedule", scheduleRepository.count());
        stats.put("byType", byType);
        
        return stats;
    }

    // 変換メソッド群
    private UnifiedRequestResponse convertHolidayToUnified(PersonalHoliday holiday) {
        UnifiedRequestResponse response = new UnifiedRequestResponse();
        response.setId(holiday.getId());
        response.setType("holiday");
        response.setUserId(holiday.getUser().getId());
        response.setUserName(holiday.getUser().getUsername());
        response.setRequestDate(holiday.getCreatedAt().toString());
        response.setStartDate(holiday.getHolidayDate().toString());
        response.setEndDate(holiday.getHolidayDate().toString());
        response.setReason(holiday.getReason());
        response.setStatus(holiday.getStatus().name().toLowerCase());
        response.setSubType(holiday.getHolidayType().name().toLowerCase());
        response.setApproverName(holiday.getApprover() != null ? holiday.getApprover().getUsername() : null);
        response.setApprovedDate(holiday.getUpdatedAt() != null ? holiday.getUpdatedAt().toString() : null);
        return response;
    }

    private UnifiedRequestResponse convertEditToUnified(EditRequest edit) {
        UnifiedRequestResponse response = new UnifiedRequestResponse();
        response.setId(edit.getId());
        response.setType("edit");
        response.setUserId(edit.getUserId());
        
        // ユーザー名を取得
        User user = userRepository.findById(edit.getUserId()).orElse(null);
        response.setUserName(user != null ? user.getUsername() : "Unknown");
        
        response.setRequestDate(edit.getRequestDate().toString());
        response.setTargetDate(edit.getTargetDate().toString());
        response.setReason(edit.getReason());
        response.setStatus(edit.getStatus().name().toLowerCase());
        response.setSubType("attendance_edit");
        
        // 勤怠修正の詳細情報
        response.setCurrentClockIn(edit.getCurrentClockIn() != null ? edit.getCurrentClockIn().toString() : null);
        response.setRequestedClockIn(edit.getRequestedClockIn() != null ? edit.getRequestedClockIn().toString() : null);
        response.setCurrentClockOut(edit.getCurrentClockOut() != null ? edit.getCurrentClockOut().toString() : null);
        response.setRequestedClockOut(edit.getRequestedClockOut() != null ? edit.getRequestedClockOut().toString() : null);
        
        // 詳細文字列を構築
        StringBuilder details = new StringBuilder();
        if (edit.getRequestedClockIn() != null) {
            details.append("出勤: ").append(edit.getCurrentClockIn()).append(" → ").append(edit.getRequestedClockIn());
        }
        if (edit.getRequestedClockOut() != null) {
            if (details.length() > 0) details.append(", ");
            details.append("退勤: ").append(edit.getCurrentClockOut()).append(" → ").append(edit.getRequestedClockOut());
        }
        response.setDetails(details.toString());
        
        // 承認者情報
        if (edit.getApproverId() != null) {
            User approver = userRepository.findById(edit.getApproverId()).orElse(null);
            response.setApproverName(approver != null ? approver.getUsername() : null);
        }
        response.setApprovedDate(edit.getApprovedDate() != null ? edit.getApprovedDate().toString() : null);
        
        return response;
    }

    private UnifiedRequestResponse convertScheduleToUnified(Schedule schedule) {
        UnifiedRequestResponse response = new UnifiedRequestResponse();
        response.setId(schedule.getId());
        response.setType("schedule");
        response.setUserId(schedule.getUserId());
        
        // ユーザー名を取得
        User user = userRepository.findById(schedule.getUserId()).orElse(null);
        response.setUserName(user != null ? user.getUsername() : "Unknown");
        
        response.setRequestDate(schedule.getDate().toString()); // スケジュール申請では日付を申請日として使用
        response.setTargetDate(schedule.getDate().toString());
        response.setReason("スケジュール申請: " + schedule.getType().name());
        response.setStatus(schedule.getStatus().name().toLowerCase());
        response.setSubType(schedule.getType().name());
        response.setDetails("勤務形態: " + schedule.getType().name());
        
        return response;
    }
}