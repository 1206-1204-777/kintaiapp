package com.example.kinntai.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    // 特定のユーザーの特定日の勤怠を取得
    Optional<Attendance> findByUserIdAndDate(Long userId, LocalDate date);
    
    // 特定のユーザーの月間勤怠を取得
    @Query("SELECT a FROM Attendance a WHERE a.userId = :userId AND EXTRACT(YEAR FROM a.date) = :year AND EXTRACT(MONTH FROM a.date) = :month ORDER BY a.date")
    List<Attendance> findMonthlyAttendances(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
    
    // 特定のユーザーの最新の勤怠を取得
    Optional<Attendance> findTopByUserIdOrderByDateDesc(Long userId);
    
    //勤怠データ取得
    List<Attendance> findByDateBetween(LocalDate start,LocalDate end);
}