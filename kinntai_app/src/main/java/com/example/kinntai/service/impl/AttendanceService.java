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

import com.example.kinntai.dto.AttendanceResponse;
import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.UserRepository;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 出勤処理
     */
    @Transactional
    public Attendance clockIn(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            LocalDate today = LocalDate.now();
            
            // 当日の勤怠記録を取得または作成
            Attendance attendance = attendanceRepository.findByUserAndDate(user, today)
                    .orElse(new Attendance(user, today));
            
            // 既に出勤済みの場合はエラー
            if (attendance.getClockIn() != null) {
                throw new RuntimeException("すでに出勤済みです");
            }
            
            // 出勤時刻を設定
            attendance.setClockIn(LocalDateTime.now());
            
            return attendanceRepository.save(attendance);
        } catch (Exception e) {
            System.err.println("出勤処理エラー: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 退勤処理
     */
    @Transactional
    public Attendance clockOut(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            LocalDate today = LocalDate.now();
            
            // 当日の勤怠記録を取得
            Attendance attendance = attendanceRepository.findByUserAndDate(user, today)
                    .orElseThrow(() -> new RuntimeException("本日の出勤記録がありません"));
            
            // 出勤していない場合はエラー
            if (attendance.getClockIn() == null) {
                throw new RuntimeException("まだ出勤していません");
            }
            
            // 既に退勤済みの場合はエラー
            if (attendance.getClockOut() != null) {
                throw new RuntimeException("すでに退勤済みです");
            }
            
            // 退勤時刻を設定
            attendance.setClockOut(LocalDateTime.now());
            
            return attendanceRepository.save(attendance);
        } catch (Exception e) {
            System.err.println("退勤処理エラー: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 現在の勤務状況を取得
     */
    public AttendanceResponse getAttendanceStatus(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            LocalDate today = LocalDate.now();
            
            // 当日の勤怠記録を取得
            Optional<Attendance> attendanceOpt = attendanceRepository.findByUserAndDate(user, today);
            
            AttendanceResponse response = new AttendanceResponse();
            response.setUserId(userId);
            response.setDate(today);
            
            if (attendanceOpt.isPresent()) {
                Attendance attendance = attendanceOpt.get();
                response.setId(attendance.getId());
                response.setClockIn(attendance.getClockIn());
                response.setClockOut(attendance.getClockOut());
                
                // 勤務中かどうかの判定
                response.setWorking(attendance.getClockIn() != null && attendance.getClockOut() == null);
            } else {
                response.setWorking(false);
            }
            
            return response;
        } catch (Exception e) {
            System.err.println("勤務状態確認エラー: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 特定の日の勤怠情報を取得
     */
    public Optional<Attendance> getAttendanceByDate(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        
        return attendanceRepository.findByUserAndDate(user, date);
    }
    
    /**
     * 月次の勤怠情報を取得
     */
    public List<Attendance> getMonthlyAttendance(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        return attendanceRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);
    }
    
    /**
     * 月次の勤怠情報を取得（文字列指定）
     */
    public List<Attendance> getMonthlyAttendance(Long userId, String yearMonth) {
        try {
            System.out.println("勤怠履歴取得: userId=" + userId + ", month=" + yearMonth);
            
            // yearMonth 形式は "YYYY-MM"
            String[] parts = yearMonth.split("-");
            if (parts.length != 2) {
                throw new IllegalArgumentException("月の形式が不正です。YYYY-MM 形式で指定してください。");
            }
            
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            
            return getMonthlyAttendance(userId, year, month);
        } catch (Exception e) {
            System.err.println("月次勤怠取得エラー: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 期間内の勤怠情報を月曜～日曜の週で生成（存在しない日も含める）
     */
    public List<Attendance> generateWeeklyAttendances(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = attendanceRepository.findByUserIdAndDateBetweenOrderByDateAsc(
                userId, startDate, endDate);
        
        List<Attendance> result = new ArrayList<>();
        
        // 存在する勤怠情報をマップに格納
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            
            // 既存の勤怠情報があればそれを使う、なければ新しく作る
            Attendance attendance = attendances.stream()
                    .filter(a -> a.getDate().equals(currentDate))
                    .findFirst()
                    .orElse(null);
            
            if (attendance == null) {
                User user = new User();
                user.setId(userId);
                
                attendance = new Attendance();
                attendance.setUser(user);
                attendance.setDate(currentDate);
            }
            
            result.add(attendance);
        }
        
        return result;
    }
}