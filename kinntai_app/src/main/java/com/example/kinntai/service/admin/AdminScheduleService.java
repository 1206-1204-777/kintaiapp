// AdminScheduleService.java
package com.example.kinntai.service.admin;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.admin.GroupedScheduleResponse;
import com.example.kinntai.dto.admin.GroupedScheduleResponse.ScheduleDetail;
import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.entity.Schedule;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.WorkType;
import com.example.kinntai.repository.ScheduleRepository;
import com.example.kinntai.repository.UserRepository;

/**
 * 管理者向けスケジュール管理サービス
 * ユーザー別・月別にグループ化したスケジュール管理
 */
@Service
@Transactional(readOnly = true)
public class AdminScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * グループ化されたスケジュール申請一覧を取得
     * 
     * @param status フィルター用ステータス
     * @return グループ化されたスケジュール一覧
     */
    public List<GroupedScheduleResponse> getGroupedScheduleRequests(String status) {
        List<Schedule> allSchedules = scheduleRepository.findAll();
        
        // ステータスでフィルタリング
        if (status != null && !status.isEmpty()) {
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
            allSchedules = allSchedules.stream()
                .filter(s -> s.getStatus() == requestStatus)
                .collect(Collectors.toList());
        }
        
        // ユーザーID + 月でグループ化
        Map<String, List<Schedule>> groupedSchedules = allSchedules.stream()
            .collect(Collectors.groupingBy(schedule -> 
                schedule.getUserId() + "-" + 
                YearMonth.from(schedule.getDate()).format(DateTimeFormatter.ofPattern("yyyy-MM"))
            ));
        
        List<GroupedScheduleResponse> result = new ArrayList<>();
        
        for (Map.Entry<String, List<Schedule>> entry : groupedSchedules.entrySet()) {
            List<Schedule> schedules = entry.getValue();
            if (schedules.isEmpty()) continue;
            
            Schedule firstSchedule = schedules.get(0);
            User user = userRepository.findById(firstSchedule.getUserId()).orElse(null);
            
            GroupedScheduleResponse grouped = new GroupedScheduleResponse();
            grouped.setUserId(firstSchedule.getUserId());
            grouped.setUserName(user != null ? user.getUsername() : "Unknown");
            grouped.setTargetMonth(YearMonth.from(firstSchedule.getDate()).format(DateTimeFormatter.ofPattern("yyyy-MM")));
            
            // 全体ステータスを判定（全て同じステータスの場合はそのステータス、混在の場合は"mixed"）
            List<RequestStatus> statuses = schedules.stream()
                .map(Schedule::getStatus)
                .distinct()
                .collect(Collectors.toList());
            
            if (statuses.size() == 1) {
                grouped.setStatus(statuses.get(0).name().toLowerCase());
            } else {
                grouped.setStatus("mixed");
            }
            
            // 統計情報を計算
            grouped.setTotalDays(schedules.size());
            grouped.setWorkDays((int) schedules.stream().filter(s -> s.getType() == WorkType.WORK).count());
            grouped.setHolidayDays((int) schedules.stream().filter(s -> s.getType() == WorkType.HOLIDAY).count());
            grouped.setRemoteDays((int) schedules.stream().filter(s -> s.getType() == WorkType.REMOTE).count());
            
            // 最初の申請日（最も古い日付）
            LocalDate earliestDate = schedules.stream()
                .map(Schedule::getDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
            grouped.setRequestDate(earliestDate.toString());
            
            // 詳細スケジュール
            List<ScheduleDetail> details = schedules.stream()
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .map(this::convertToScheduleDetail)
                .collect(Collectors.toList());
            grouped.setScheduleDetails(details);
            
            result.add(grouped);
        }
        
        // 申請日順（新しい順）でソート
        return result.stream()
            .sorted((a, b) -> b.getRequestDate().compareTo(a.getRequestDate()))
            .collect(Collectors.toList());
    }

    /**
     * 承認待ちのグループ化スケジュール申請を取得
     */
    public List<GroupedScheduleResponse> getPendingGroupedSchedules() {
        return getGroupedScheduleRequests("pending");
    }

    /**
     * ユーザーの月間スケジュールを一括承認
     * 
     * @param userId ユーザーID
     * @param targetMonth 対象月 (YYYY-MM)
     * @param approverId 承認者ID
     */
    @Transactional
    public void approveUserMonthlySchedule(Long userId, String targetMonth, Long approverId) {
        try {
            // targetMonthの形式検証とパース
            if (targetMonth == null || !targetMonth.matches("\\d{4}-\\d{2}")) {
                throw new IllegalArgumentException("Invalid target month format: " + targetMonth + ". Expected YYYY-MM format.");
            }
            
            YearMonth yearMonth = YearMonth.parse(targetMonth);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();
            
            List<Schedule> schedules = scheduleRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
            
            // 承認待ちのスケジュールのみを承認
            int approvedCount = 0;
            for (Schedule schedule : schedules) {
                if (schedule.getStatus() == RequestStatus.PENDING) {
                    schedule.setStatus(RequestStatus.APPROVED);
                    scheduleRepository.save(schedule);
                    approvedCount++;
                }
            }
            
            System.out.println("ユーザー" + userId + "の" + targetMonth + "月スケジュール" + approvedCount + "件を承認しました");
            
        } catch (DateTimeParseException e) {
            System.err.println("日付フォーマットエラー: " + targetMonth + " - " + e.getMessage());
            throw new IllegalArgumentException("Invalid date format: " + targetMonth, e);
        } catch (Exception e) {
            System.err.println("スケジュール承認エラー: " + e.getMessage());
            throw e;
        }
    }

    /**
     * ユーザーの月間スケジュールを一括却下
     * 
     * @param userId ユーザーID
     * @param targetMonth 対象月 (YYYY-MM)
     * @param approverId 承認者ID
     */
    @Transactional
    public void rejectUserMonthlySchedule(Long userId, String targetMonth, Long approverId) {
        try {
            // targetMonthの形式検証とパース
            if (targetMonth == null || !targetMonth.matches("\\d{4}-\\d{2}")) {
                throw new IllegalArgumentException("Invalid target month format: " + targetMonth + ". Expected YYYY-MM format.");
            }
            
            YearMonth yearMonth = YearMonth.parse(targetMonth);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();
            
            List<Schedule> schedules = scheduleRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
            
            // 承認待ちのスケジュールのみを却下
            int rejectedCount = 0;
            for (Schedule schedule : schedules) {
                if (schedule.getStatus() == RequestStatus.PENDING) {
                    schedule.setStatus(RequestStatus.REJECTED);
                    scheduleRepository.save(schedule);
                    rejectedCount++;
                }
            }
            
            System.out.println("ユーザー" + userId + "の" + targetMonth + "月スケジュール" + rejectedCount + "件を却下しました");
            
        } catch (DateTimeParseException e) {
            System.err.println("日付フォーマットエラー: " + targetMonth + " - " + e.getMessage());
            throw new IllegalArgumentException("Invalid date format: " + targetMonth, e);
        } catch (Exception e) {
            System.err.println("スケジュール却下エラー: " + e.getMessage());
            throw e;
        }
    }

    /**
     * スケジュール統計情報を取得
     */
    public Map<String, Object> getScheduleStatistics() {
        List<GroupedScheduleResponse> grouped = getGroupedScheduleRequests(null);
        
        long pendingCount = grouped.stream()
            .filter(g -> "pending".equals(g.getStatus()))
            .count();
            
        long approvedCount = grouped.stream()
            .filter(g -> "approved".equals(g.getStatus()))
            .count();
            
        long rejectedCount = grouped.stream()
            .filter(g -> "rejected".equals(g.getStatus()))
            .count();
        
        return Map.of(
            "totalGroups", grouped.size(),
            "pendingGroups", pendingCount,
            "approvedGroups", approvedCount,
            "rejectedGroups", rejectedCount
        );
    }

    // ヘルパーメソッド
    private ScheduleDetail convertToScheduleDetail(Schedule schedule) {
        ScheduleDetail detail = new ScheduleDetail();
        detail.setId(schedule.getId());
        detail.setDate(schedule.getDate().toString());
        detail.setType(schedule.getType().name());
        detail.setStatus(schedule.getStatus().name().toLowerCase());
        return detail;
    }
}