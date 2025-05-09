package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.WorkingHour;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.repository.WorkingHourRepository;

@Service
public class AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WorkingHourRepository workingHourRepository;
    /**
     * 出勤打刻
     */
    @Transactional
    public Attendance clockIn(Long userId) {
        // ユーザーの存在確認
        User user = getUserByUserId(userId);
        
        // 今日の日付
        LocalDate today = LocalDate.now();
        
        // 既存の勤怠記録を確認
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndDate(userId, today);
        
        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            
            // すでに出勤打刻済みの場合
            if (attendance.getClockIn() != null) {
                throw new RuntimeException("すでに出勤打刻されています");
            }
            
            // 出勤時間を設定
            attendance.setClockIn(LocalDateTime.now());
            return attendanceRepository.save(attendance);
        } else {
            // 新しい勤怠記録を作成
            Attendance attendance = new Attendance();
            attendance.setUserId(userId);
            attendance.setDate(today);
            attendance.setClockIn(LocalDateTime.now());
            
            return attendanceRepository.save(attendance);
        }
    }
    
    /**
     * 退勤打刻
     */
    @Transactional
    public Attendance clockOut(Long userId) {
        // ユーザーの存在確認
        User user = getUserByUserId(userId);
        
        // 今日の日付
        LocalDate today = LocalDate.now();
        
        // 勤怠記録を確認
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndDate(userId, today);
        
        if (existingAttendance.isEmpty()) {
            throw new RuntimeException("出勤打刻がありません");
        }
        
        Attendance attendance = existingAttendance.get();
        
        // 出勤打刻が存在しない場合
        if (attendance.getClockIn() == null) {
            throw new RuntimeException("出勤打刻がありません");
        }
        
        // すでに退勤打刻済みの場合
        if (attendance.getClockOut() != null) {
            throw new RuntimeException("すでに退勤打刻されています");
        }
        
        // 退勤時間を設定
        attendance.setClockOut(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }
    
    /**
     * 月別勤怠取得
     */
    public List<Attendance> getMonthlyAttendances(Long userId, String month) {
        // ユーザーの存在確認
        User user = getUserByUserId(userId);
        
        // 月の解析
        YearMonth yearMonth = YearMonth.parse(month);
        int year = yearMonth.getYear();
        int monthValue = yearMonth.getMonthValue();
        
        // データを取得
        List<Attendance> attendances = attendanceRepository.findMonthlyAttendances(userId, year, monthValue);
        
        // 月の全日付分のデータを用意
        List<Attendance> result = new ArrayList<>();
        
        // 月の各日についてデータを準備
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            
            // すでにデータがある日付はそのまま使用
            boolean found = false;
            for (Attendance attendance : attendances) {
                if (attendance.getDate().equals(date)) {
                    result.add(attendance);
                    found = true;
                    break;
                }
            }
            
            // データがない日付は空のデータを作成
            if (!found) {
                Attendance emptyAttendance = new Attendance();
                emptyAttendance.setUserId(userId);
                emptyAttendance.setDate(date);
                result.add(emptyAttendance);
            }
        }
        
        return result;
    }
    
    /**
     * 特定日の勤怠取得
     */
    public Attendance getAttendanceByDate(Long userId, LocalDate date) {
        // ユーザーの存在確認
        User user = getUserByUserId(userId);
        
        // データを取得
        Optional<Attendance> attendance = attendanceRepository.findByUserIdAndDate(userId, date);
        
        return attendance.orElse(null);
    }
    
    /**
     * 現在の勤怠状態を確認
     */
    public boolean isWorking(Long userId) {
        // ユーザーの存在確認
        User user = getUserByUserId(userId);
        
        // 今日の日付
        LocalDate today = LocalDate.now();
        
        // 勤怠記録を確認
        Optional<Attendance> attendance = attendanceRepository.findByUserIdAndDate(userId, today);
        
        // 出勤済みで退勤していない場合は勤務中
        return attendance.isPresent() && 
               attendance.get().getClockIn() != null && 
               attendance.get().getClockOut() == null;
    }
    
    /**
     * ユーザーIDからユーザーを取得
     */
    private User getUserByUserId(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + userId));
    }
    
    public Optional<WorkingHour> getWorkingHour(Long userId, LocalDate date) {
        return workingHourRepository.findTopByUserIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(userId, date);
    }

}