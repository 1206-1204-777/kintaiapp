package com.example.kinntai.service.admin;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.kinntai.dto.admin.AttendanceStatsResponse;
import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.OvertimeRequest; // OvertimeRequestをインポート
import com.example.kinntai.entity.PersonalHoliday;
import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.OvertimeRepository; // OvertimeRepositoryをインポート
import com.example.kinntai.repository.PersonalHolidayRepository;
import com.example.kinntai.repository.UserRepository;


/**
 * 管理者ダッシュボードサービス
 * ダッシュボード表示に必要な統計情報や概要データの計算・集約を行う
 */
@Service
public class AdminDashboardService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonalHolidayRepository personalHolidayRepository;

    @Autowired
    private OvertimeRepository overtimeRepository; // 修正: OvertimeRequestRepositoryからOvertimeRepositoryに変更

    @Autowired
    private AdminAttendanceService adminAttendanceService;

    @Autowired
    private AdminHolidayRequestService adminHolidayRequestService;

    /**
     * ダッシュボード用の統合統計データを取得
     * 勤怠統計、休暇申請統計、アラート情報などを一括で提供
     *
     * @return 統合ダッシュボードデータ
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // 勤怠統計 (AdminAttendanceServiceで管理者を除外したものが取得される)
        AttendanceStatsResponse attendanceStats = adminAttendanceService.getTodayAttendanceStats();
        dashboardData.put("attendanceStats", attendanceStats);
        
        // 休暇申請統計
        Map<String, Object> holidayStats = adminHolidayRequestService.getHolidayRequestStats();
        dashboardData.put("holidayStats", holidayStats);
        
        // システムアラート
        Map<String, Object> alerts = getSystemAlerts();
        dashboardData.put("alerts", alerts);
        
        // 月次トレンド（簡易版）
        Map<String, Object> trends = getSimpleTrends();
        dashboardData.put("trends", trends);

        // 今日の申請数を取得 (個人休暇申請と残業申請の合計)
        long todayRequestsCount = getTodayTotalRequestsCount(); // メソッド名変更
        dashboardData.put("todayRequestsCount", todayRequestsCount); // 新しく追加
        
        
        

        return dashboardData;
    }

    /**
     * 今日の個人休暇申請と残業申請の合計数を取得 (管理者ユーザーからの申請を除く)
     * @return 今日の総申請数
     */
    private long getTodayTotalRequestsCount() {
        // 1. 休日申請の承認待ち件数
        long personalHolidayPendingCount = personalHolidayRepository.findAll().stream()
            .filter(h -> h.getStatus() == RequestStatus.PENDING)  // ✅ createdAtの判定を削除
            .filter(h -> !h.getUser().getRole().name().equals("ADMIN"))
            .count();

        // 2. 残業申請の承認待ち件数
        long overtimePendingCount = overtimeRepository.findAll().stream()
            .filter(o -> o.getStatus() == RequestStatus.PENDING)  // ✅ createdAtの判定を削除
            .filter(o -> {
                User user = userRepository.findById(o.getUserId()).orElse(null);
                return user != null && !user.getRole().name().equals("ADMIN");
            })
            .count();

        // ✅ 承認待ち全件を合計
        return personalHolidayPendingCount + overtimePendingCount;
    }


    /**
     * システムアラート情報を取得
     * 管理者が注意すべき項目をチェック
     * (管理者ユーザーを除外して計算)
     *
     * @return アラート情報
     */
    public Map<String, Object> getSystemAlerts() {
        Map<String, Object> alerts = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        // 未退勤者数 (管理者ユーザーを除外)
        List<Attendance> unclockedOut = attendanceRepository.findByDateAndClockInNotNullAndClockOutIsNull(today).stream()
                                            .filter(a -> !a.getUser().getRole().name().equals("ADMIN"))
                                            .collect(Collectors.toList());
        alerts.put("unclockedOutCount", unclockedOut.size());
        
        // 承認待ち休暇申請数 (管理者ユーザーを除外 - PersonalHolidayにはUser情報があるため)
        List<PersonalHoliday> pendingRequests = personalHolidayRepository.findAll().stream()
                .filter(h -> !h.getUser().getRole().name().equals("ADMIN")) // 申請ユーザーが管理者でないことを確認
                .filter(h -> h.getStatus() == RequestStatus.PENDING)
                .collect(Collectors.toList());
        
        // 承認待ち残業申請数 (管理者ユーザーを除外)
        List<OvertimeRequest> pendingOvertimeRequests = overtimeRepository.findAll().stream() // 修正: overtimeRequestRepositoryからovertimeRepositoryに変更
                .filter(o -> o.getStatus() == RequestStatus.PENDING)
                .filter(o -> {
                    User user = userRepository.findById(o.getUserId()).orElse(null);
                    return user != null && !user.getRole().name().equals("ADMIN");
                })
                .collect(Collectors.toList());

        alerts.put("pendingRequestsCount", pendingRequests.size() + pendingOvertimeRequests.size()); // 合計数を設定
        
        // 長時間残業者数（8時間以上の残業） (管理者ユーザーを除外)
        List<Attendance> longOvertimeToday = attendanceRepository.findByDateBetween(today, today).stream()
                .filter(a -> !a.getUser().getRole().name().equals("ADMIN")) // 管理者ユーザーを除外
                .filter(a -> a.getOvertimeMinutes() != null && a.getOvertimeMinutes() >= 480) // 8時間 = 480分
                .collect(Collectors.toList());
        alerts.put("longOvertimeCount", longOvertimeToday.size());
        
        // 今週の平均残業時間 (管理者ユーザーを除外)
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        List<Attendance> thisWeekAttendances = attendanceRepository.findByDateBetween(weekStart, today).stream()
                                                    .filter(a -> !a.getUser().getRole().name().equals("ADMIN")) // 管理者ユーザーを除外
                                                    .collect(Collectors.toList());
        double avgOvertimeThisWeek = thisWeekAttendances.stream()
                .filter(a -> a.getOvertimeMinutes() != null)
                .mapToLong(Attendance::getOvertimeMinutes)
                .average()
                .orElse(0.0) / 60.0; // 時間に変換
        alerts.put("avgOvertimeThisWeek", Math.round(avgOvertimeThisWeek * 100.0) / 100.0);
        
        return alerts;
    }

    /**
     * 月次サマリー統計を取得
     * 過去3ヶ月の勤怠トレンドデータ (管理者ユーザーを除外して計算)
     *
     * @return 月次サマリー統計
     */
    public Map<String, Object> getMonthlySummary() {
        Map<String, Object> summary = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        // 過去3ヶ月のデータを取得
        for (int i = 0; i < 3; i++) {
            LocalDate targetMonth = today.minusMonths(i);
            LocalDate monthStart = targetMonth.withDayOfMonth(1);
            LocalDate monthEnd = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());
            
            List<Attendance> monthlyAttendances = attendanceRepository.findByDateBetween(monthStart, monthEnd).stream()
                                                        .filter(a -> !a.getUser().getRole().name().equals("ADMIN")) // 管理者ユーザーを除外
                                                        .collect(Collectors.toList());
            
            // 月次統計計算
            Map<String, Object> monthStats = new HashMap<>();
            
            long totalWorkDays = monthlyAttendances.stream()
                    .filter(a -> a.getClockIn() != null)
                    .count();
            
            double totalWorkHours = monthlyAttendances.stream()
                    .filter(a -> a.getTotalWorkMin() != null)
                    .mapToLong(Attendance::getTotalWorkMin)
                    .sum() / 60.0;
            
            double totalOvertimeHours = monthlyAttendances.stream()
                    .filter(a -> a.getOvertimeMinutes() != null)
                    .mapToLong(Attendance::getOvertimeMinutes)
                    .sum() / 60.0;
            
            double avgWorkHours = totalWorkDays > 0 ? totalWorkHours / totalWorkDays : 0.0;
            double avgOvertimeHours = totalWorkDays > 0 ? totalOvertimeHours / totalWorkDays : 0.0;
            
            monthStats.put("workDays", totalWorkDays);
            monthStats.put("totalWorkHours", Math.round(totalWorkHours * 100.0) / 100.0);
            monthStats.put("totalOvertimeHours", Math.round(totalOvertimeHours * 100.0) / 100.0);
            monthStats.put("avgWorkHours", Math.round(avgWorkHours * 100.0) / 100.0);
            monthStats.put("avgOvertimeHours", Math.round(avgOvertimeHours * 100.0) / 100.0);
            
            String monthKey = targetMonth.getYear() + "-" + String.format("%02d", targetMonth.getMonthValue());
            summary.put(monthKey, monthStats);
        }
        
        return summary;
    }

    /**
     * 部署別統計を取得
     * 各部署の勤怠状況比較データ (管理者ユーザーを除外して計算)
     *
     * @return 部署別統計データ
     */
    public Map<String, Object> getDepartmentStats() {
        Map<String, Object> departmentStats = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        
        // 今月の勤怠データを取得し、管理者ユーザーを除外
        List<Attendance> monthlyAttendances = attendanceRepository.findByDateBetween(monthStart, today).stream()
                                                    .filter(a -> !a.getUser().getRole().name().equals("ADMIN")) // 管理者ユーザーを除外
                                                    .collect(Collectors.toList());
        
        // 部署別にグループ化
        Map<String, List<Attendance>> departmentGroups = monthlyAttendances.stream()
                .collect(Collectors.groupingBy(a -> 
                    a.getUser().getLocation() != null ? 
                    a.getUser().getLocation().getName() : "未設定"));
        
        for (Map.Entry<String, List<Attendance>> entry : departmentGroups.entrySet()) {
            String department = entry.getKey();
            List<Attendance> deptAttendances = entry.getValue();
            
            Map<String, Object> deptStats = new HashMap<>();
            
            // 部署の社員数 (管理者ユーザーを除外)
            long employeeCount = deptAttendances.stream()
                    .map(a -> a.getUser())
                    .filter(user -> !user.getRole().name().equals("ADMIN")) // ここでも管理者を除外
                    .distinct()
                    .count();
            
            // 今日の出勤率 (管理者ユーザーを除外)
            List<Attendance> todayAttendances = deptAttendances.stream()
                    .filter(a -> a.getDate().equals(today))
                    .filter(a -> !a.getUser().getRole().name().equals("ADMIN")) // ここでも管理者を除外
                    .collect(Collectors.toList());
            
            long todayPresentCount = todayAttendances.stream()
                    .filter(a -> a.getClockIn() != null)
                    .count();
            
            double attendanceRate = employeeCount > 0 ? 
                    (double) todayPresentCount / employeeCount * 100.0 : 0.0;
            
            // 平均残業時間 (管理者ユーザーを除外)
            double avgOvertimeHours = deptAttendances.stream()
                    .filter(a -> !a.getUser().getRole().name().equals("ADMIN")) // ここでも管理者を除外
                    .filter(a -> a.getOvertimeMinutes() != null)
                    .mapToLong(Attendance::getOvertimeMinutes)
                    .average()
                    .orElse(0.0) / 60.0;
            
            // 総勤務時間 (管理者ユーザーを除外)
            double totalWorkHours = deptAttendances.stream()
                    .filter(a -> !a.getUser().getRole().name().equals("ADMIN")) // ここでも管理者を除外
                    .filter(a -> a.getTotalWorkMin() != null)
                    .mapToLong(Attendance::getTotalWorkMin)
                    .sum() / 60.0;
            
            deptStats.put("employeeCount", employeeCount);
            deptStats.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
            deptStats.put("avgOvertimeHours", Math.round(avgOvertimeHours * 100.0) / 100.0);
            deptStats.put("totalWorkHours", Math.round(totalWorkHours * 100.0) / 100.0);
            deptStats.put("todayPresentCount", todayPresentCount);
            
            departmentStats.put(department, deptStats);
        }
        
        return departmentStats;
    }

    /**
     * 簡易トレンドデータを取得
     * ダッシュボード用の基本的なトレンド情報 (管理者ユーザーを除外して計算)
     *
     * @return 簡易トレンドデータ
     */
    private Map<String, Object> getSimpleTrends() {
        Map<String, Object> trends = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        // 過去7日間の出勤率トレンド
        Map<String, Double> attendanceRateTrend = new HashMap<>();
        // 全ユーザーから管理者を除外したリストを取得
        List<User> filteredUsers = userRepository.findAll().stream()
                                    .filter(user -> !user.getRole().name().equals("ADMIN"))
                                    .collect(Collectors.toList());
        int totalUsers = filteredUsers.size(); // フィルター後の社員数を使用
        
        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            // その日の勤怠データから管理者ユーザーのものを除外
            List<Attendance> dayAttendances = attendanceRepository.findByDateBetween(targetDate, targetDate).stream()
                                                    .filter(a -> !a.getUser().getRole().name().equals("ADMIN"))
                                                    .collect(Collectors.toList());
            
            long presentCount = dayAttendances.stream()
                    .filter(a -> a.getClockIn() != null)
                    .count();
            
            double rate = totalUsers > 0 ? (double) presentCount / totalUsers * 100.0 : 0.0;
            attendanceRateTrend.put(targetDate.toString(), Math.round(rate * 100.0) / 100.0);
        }
        
        // 7日間の平均残業時間トレンド
        Map<String, Double> overtimeTrend = new HashMap<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            // その日の勤怠データから管理者ユーザーのものを除外
            List<Attendance> dayAttendances = attendanceRepository.findByDateBetween(targetDate, targetDate).stream()
                                                    .filter(a -> !a.getUser().getRole().name().equals("ADMIN"))
                                                    .collect(Collectors.toList());
            
            double avgOvertime = dayAttendances.stream()
                    .filter(a -> a.getOvertimeMinutes() != null)
                    .mapToLong(Attendance::getOvertimeMinutes)
                    .average()
                    .orElse(0.0) / 60.0;
            
            overtimeTrend.put(targetDate.toString(), Math.round(avgOvertime * 100.0) / 100.0);
        }
        
        trends.put("attendanceRateTrend", attendanceRateTrend);
        trends.put("overtimeTrend", overtimeTrend);
        
        return trends;
    }

    /**
     * 今週と先週の比較統計を取得
     * パフォーマンス比較用 (管理者ユーザーを除外して計算)
     *
     * @return 週次比較統計
     */
    public Map<String, Object> getWeeklyComparison() {
        Map<String, Object> comparison = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        // 今週の範囲
        LocalDate thisWeekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate thisWeekEnd = today;
        
        // 先週の範囲
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekStart.minusDays(1);
        
        // 今週のデータ (管理者ユーザーを除外)
        List<Attendance> thisWeekAttendances = attendanceRepository.findByDateBetween(thisWeekStart, thisWeekEnd).stream()
                                                    .filter(a -> !a.getUser().getRole().name().equals("ADMIN"))
                                                    .collect(Collectors.toList());
        Map<String, Object> thisWeekStats = calculateWeeklyStats(thisWeekAttendances);
        
        // 先週のデータ (管理者ユーザーを除外)
        List<Attendance> lastWeekAttendances = attendanceRepository.findByDateBetween(lastWeekStart, lastWeekEnd).stream()
                                                    .filter(a -> !a.getUser().getRole().name().equals("ADMIN"))
                                                    .collect(Collectors.toList());
        Map<String, Object> lastWeekStats = calculateWeeklyStats(lastWeekAttendances);
        
        comparison.put("thisWeek", thisWeekStats);
        comparison.put("lastWeek", lastWeekStats);
        
        // 増減率計算
        Map<String, Object> changes = new HashMap<>();
        
        double thisWeekAvgWork = (Double) thisWeekStats.get("avgWorkHours");
        double lastWeekAvgWork = (Double) lastWeekStats.get("avgWorkHours");
        double workHoursChange = lastWeekAvgWork > 0 ? 
                ((thisWeekAvgWork - lastWeekAvgWork) / lastWeekAvgWork * 100.0) : 0.0;
        
        double thisWeekAvgOvertime = (Double) thisWeekStats.get("avgOvertimeHours");
        double lastWeekAvgOvertime = (Double) lastWeekStats.get("avgOvertimeHours");
        double overtimeChange = lastWeekAvgOvertime > 0 ? 
                ((thisWeekAvgOvertime - lastWeekAvgOvertime) / lastWeekAvgOvertime * 100.0) : 0.0;
        
        changes.put("workHoursChange", Math.round(workHoursChange * 100.0) / 100.0);
        changes.put("overtimeChange", Math.round(overtimeChange * 100.0) / 100.0);
        
        return comparison;
    }

    /**
     * 週次統計を計算
     * @param attendances 対象期間の勤怠データ
     * @return 週次統計
     */
    private Map<String, Object> calculateWeeklyStats(List<Attendance> attendances) {
        Map<String, Object> stats = new HashMap<>();
        
        long workDays = attendances.stream()
                .filter(a -> a.getClockIn() != null)
                .count();
        
        double totalWorkHours = attendances.stream()
                .filter(a -> a.getTotalWorkMin() != null)
                .mapToLong(Attendance::getTotalWorkMin)
                .sum() / 60.0;
        
        double totalOvertimeHours = attendances.stream()
                .filter(a -> a.getOvertimeMinutes() != null)
                .mapToLong(Attendance::getOvertimeMinutes)
                .sum() / 60.0;
        
        double avgWorkHours = workDays > 0 ? totalWorkHours / workDays : 0.0;
        double avgOvertimeHours = workDays > 0 ? totalOvertimeHours / workDays : 0.0;
        
        stats.put("workDays", workDays);
        stats.put("totalWorkHours", Math.round(totalWorkHours * 100.0) / 100.0);
        stats.put("totalOvertimeHours", Math.round(totalOvertimeHours * 100.0) / 100.0);
        stats.put("avgWorkHours", Math.round(avgWorkHours * 100.0) / 100.0);
        stats.put("avgOvertimeHours", Math.round(avgOvertimeHours * 100.0) / 100.0);
        
        return stats;
    }
}
