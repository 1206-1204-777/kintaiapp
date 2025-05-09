package com.example.kinntai.service.impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.MonthlySummary;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.WeeklySummary;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.HolidayRepository;
import com.example.kinntai.repository.MonthlySummaryRepository;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.repository.WeeklySummaryRepository;

@Service
public class SummaryService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private MonthlySummaryRepository monthlySummaryRepository;
    
    @Autowired
    private WeeklySummaryRepository weeklySummaryRepository;
    
    @Autowired
    private HolidayRepository holidayRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // 標準勤務時間（8時間）
    private static final double STANDARD_WORK_HOURS = 8.0;
    
    /**
     * 月次集計バッチ処理
     * 前月の集計を行う
     */
    @Transactional
    public void executeMonthlyBatch() {
        // 前月のYearMonthを取得
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        int year = lastMonth.getYear();
        int month = lastMonth.getMonthValue();
        
        // 全ユーザーを取得
        List<User> allUsers = userRepository.findAll();
        
        // 各ユーザーの集計を実行
        for (User user : allUsers) {
            calculateMonthlySummary(user.getUserId(), year, month);
        }
    }
    
    /**
     * 週次集計バッチ処理
     * 前週の集計を行う
     */
    @Transactional
    public void executeWeeklyBatch() {
        // 現在の日付
        LocalDate today = LocalDate.now();
        
        // 前週の開始日と終了日を計算
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeekNumber = today.get(weekFields.weekOfWeekBasedYear());
        
        // 前週の月曜日を取得
        LocalDate startOfLastWeek = today.minusWeeks(1)
                .with(ChronoField.DAY_OF_WEEK, 1); // 月曜日
        
        // 前週の日曜日を取得
        LocalDate endOfLastWeek = startOfLastWeek.plusDays(6); // 日曜日
        
        // 前週の年、月、週番号を取得
        int year = startOfLastWeek.getYear();
        int month = startOfLastWeek.getMonthValue();
        int weekNumber = startOfLastWeek.get(weekFields.weekOfWeekBasedYear());
        
        // 全ユーザーを取得
        List<User> allUsers = userRepository.findAll();
        
        // 各ユーザーの集計を実行
        for (User user : allUsers) {
            calculateWeeklySummary(user.getUserId(), year, month, weekNumber, startOfLastWeek, endOfLastWeek);
        }
    }
    
    /**
     * 特定ユーザーの特定年月の月次集計を計算
     */
    @Transactional
    public MonthlySummary calculateMonthlySummary(Long userId, int year, int month) {
        // ユーザーの存在確認
        User user = getUserByUserId(userId);
        
        // 年月の期間を取得
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // 既存の集計があれば取得
        Optional<MonthlySummary> existingSummary = monthlySummaryRepository.findByUserIdAndYearAndMonth(userId, year, month);
        
        MonthlySummary summary;
        if (existingSummary.isPresent()) {
            summary = existingSummary.get();
        } else {
            summary = new MonthlySummary();
            summary.setUserId(userId);
            summary.setYear(year);
            summary.setMonth(month);
        }
        
        // 期間内の勤怠データを取得
        List<Attendance> attendances = attendanceRepository.findMonthlyAttendances(userId, year, month);
        
        // 集計値の計算
        int workDays = 0;
        double totalWorkHours = 0.0;
        double overtimeHours = 0.0;
        
        // 営業日数を計算（土日と祝日を除く）
        int businessDays = countBusinessDays(startDate, endDate);
        
        // 出勤日と勤務時間を集計
        for (Attendance attendance : attendances) {
            // 出勤と退勤の両方が記録されている場合のみカウント
            if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
                workDays++;
                
                // 勤務時間を計算（時間単位）
                double hours = calculateWorkHours(attendance.getClockIn(), attendance.getClockOut());
                totalWorkHours += hours;
                
                // 残業時間を計算（8時間を超える分）
                double overtime = Math.max(0, hours - STANDARD_WORK_HOURS);
                overtimeHours += overtime;
            }
        }
        
        // 休日数を取得
        int holidayCount = holidayRepository.countHolidaysInMonth(year, month);
        
        // 欠勤日数を計算
        int absentDays = businessDays - workDays;
        
        // 集計結果を設定
        summary.setWorkDays(workDays);
        summary.setTotalWorkHours(totalWorkHours);
        summary.setAverageWorkHours(workDays > 0 ? totalWorkHours / workDays : 0);
        summary.setOvertimeHours(overtimeHours);
        summary.setAbsentDays(absentDays);
        summary.setHolidayCount(holidayCount);
        
        // 保存
        return monthlySummaryRepository.save(summary);
    }
    
    /**
     * 特定ユーザーの特定週の週次集計を計算
     */
    @Transactional
    public WeeklySummary calculateWeeklySummary(Long userId, int year, int month, int weekNumber, 
                                                LocalDate startDate, LocalDate endDate) {
        // ユーザーの存在確認
        User user = getUserByUserId(userId);
        
        // 既存の集計があれば取得（IDのみの検索ではなく、複合条件での検索が必要）
        List<WeeklySummary> existingSummaries = weeklySummaryRepository.findByUserIdAndYearAndMonthOrderByWeekNumber(userId, year, month);
        
        // 該当する週の集計を探す
        WeeklySummary summary = null;
        for (WeeklySummary s : existingSummaries) {
            if (s.getWeekNumber() == weekNumber) {
                summary = s;
                break;
            }
        }
        
        // 存在しなければ新規作成
        if (summary == null) {
            summary = new WeeklySummary();
            summary.setUserId(userId);
            summary.setYear(year);
            summary.setMonth(month);
            summary.setWeekNumber(weekNumber);
            summary.setStartDate(startDate);
            summary.setEndDate(endDate);
        }
        
        // 期間内のすべての日付の勤怠データを取得
        List<Attendance> weeklyAttendances = new ArrayList<>();
        
        // 指定された期間の各日について勤怠データを取得
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Optional<Attendance> attendance = attendanceRepository.findByUserIdAndDate(userId, currentDate);
            attendance.ifPresent(weeklyAttendances::add);
            currentDate = currentDate.plusDays(1);
        }
        
        // 集計値の計算
        int workDays = 0;
        double totalWorkHours = 0.0;
        double overtimeHours = 0.0;
        
        // 出勤日と勤務時間を集計
        for (Attendance attendance : weeklyAttendances) {
            // 出勤と退勤の両方が記録されている場合のみカウント
            if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
                workDays++;
                
                // 勤務時間を計算（時間単位）
                double hours = calculateWorkHours(attendance.getClockIn(), attendance.getClockOut());
                totalWorkHours += hours;
                
                // 残業時間を計算（8時間を超える分）
                double overtime = Math.max(0, hours - STANDARD_WORK_HOURS);
                overtimeHours += overtime;
            }
        }
        
        // 集計結果を設定
        summary.setWorkDays(workDays);
        summary.setTotalWorkHours(totalWorkHours);
        summary.setAverageWorkHours(workDays > 0 ? totalWorkHours / workDays : 0);
        summary.setOvertimeHours(overtimeHours);
        
        // 保存
        return weeklySummaryRepository.save(summary);
    }
    
    /**
     * 特定ユーザーの月次集計を取得
     */
    public MonthlySummary getMonthlySummary(Long userId, int year, int month) {
        // ユーザーの存在確認
        User user = getUserByUserId(userId);
        
        // 集計データを取得
        Optional<MonthlySummary> summary = monthlySummaryRepository.findByUserIdAndYearAndMonth(userId, year, month);
        
        // 存在しない場合は計算して返す
        if (summary.isEmpty()) {
            return calculateMonthlySummary(userId, year, month);
        }
        
        return summary.get();
    }
    
    /**
     * 特定ユーザーの週次集計を取得
     */
    public List<WeeklySummary> getWeeklySummaries(Long userId, int year, int month) {
        // ユーザーの存在確認
        User user = getUserByUserId(userId);
        
        // 集計データを取得
        List<WeeklySummary> summaries = weeklySummaryRepository.findByUserIdAndYearAndMonthOrderByWeekNumber(userId, year, month);
        
        // もし該当月のデータがなければ空のリストを返す
        return summaries;
    }
    
    /**
     * 特定ユーザーの月間サマリーデータを取得
     */
    public Map<String, Object> getMonthlySummaryData(Long userId, String monthStr) {
        // 年月の解析
        YearMonth yearMonth = YearMonth.parse(monthStr);
        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();
        
        // 月次集計を取得
        MonthlySummary monthlySummary = getMonthlySummary(userId, year, month);
        
        // 週次集計を取得
        List<WeeklySummary> weeklySummaries = getWeeklySummaries(userId, year, month);
        
        // レスポンス用のマップを作成
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("year", year);
        response.put("month", month);
        response.put("workDaysCount", monthlySummary.getWorkDays());
        response.put("totalWorkHours", monthlySummary.getTotalWorkHours());
        response.put("avgWorkHours", monthlySummary.getAverageWorkHours());
        response.put("overtimeHours", monthlySummary.getOvertimeHours());
        response.put("absentDays", monthlySummary.getAbsentDays());
        response.put("holidayCount", monthlySummary.getHolidayCount());
        
        // 週次サマリーをマップのリストに変換
        List<Map<String, Object>> weeklyData = weeklySummaries.stream()
                .map(weekly -> {
                    Map<String, Object> weekMap = new HashMap<>();
                    weekMap.put("weekNumber", weekly.getWeekNumber());
                    weekMap.put("startDate", weekly.getStartDate().toString());
                    weekMap.put("endDate", weekly.getEndDate().toString());
                    weekMap.put("workDaysCount", weekly.getWorkDays());
                    weekMap.put("totalWorkHours", weekly.getTotalWorkHours());
                    weekMap.put("avgWorkHours", weekly.getAverageWorkHours());
                    weekMap.put("overtimeHours", weekly.getOvertimeHours());
                    return weekMap;
                })
                .collect(Collectors.toList());
        
        response.put("weeklySummaries", weeklyData);
        
        return response;
    }
    
    /**
     * 営業日数を計算（土日と祝日を除く）
     */
    private int countBusinessDays(LocalDate startDate, LocalDate endDate) {
        int businessDays = 0;
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            // 土日でなく、祝日でもない場合にカウント
            if (!isWeekend(currentDate) && !isHoliday(currentDate)) {
                businessDays++;
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return businessDays;
    }
    
    /**
     * 土日かどうかを判定
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * 祝日かどうかを判定
     */
    private boolean isHoliday(LocalDate date) {
        return holidayRepository.existsByDate(date);
    }
    
    /**
     * 勤務時間を計算（時間単位）
     */
    private double calculateWorkHours(LocalDateTime clockIn, LocalDateTime clockOut) {
        if (clockIn == null || clockOut == null) {
            return 0.0;
        }
        
        Duration duration = Duration.between(clockIn, clockOut);
        long minutes = duration.toMinutes();
        
        // 分を時間に変換（小数点以下2桁まで）
        return Math.round((minutes / 60.0) * 100.0) / 100.0;
    }
    
    /**
     * ユーザーIDからユーザーを取得
     */
    private User getUserByUserId(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + userId));
    }
}