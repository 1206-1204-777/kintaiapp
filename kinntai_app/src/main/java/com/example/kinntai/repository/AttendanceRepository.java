package com.example.kinntai.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.User;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    Optional<Attendance> findByUserAndDate(User user, LocalDate date);
    
    List<Attendance> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.user.id = ?1 AND YEAR(a.date) = ?2 AND MONTH(a.date) = ?3 ORDER BY a.date ASC")
    List<Attendance> findByUserIdAndYearAndMonth(Long userId, int year, int month);
    
    List<Attendance> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    /*退勤していないユーザーを見つける*/
    @Query("SELECT a FROM Attendance a WHERE a.date = :today AND a.clockIn IS NOT NULL AND a.clockOut IS NULL")
    List<Attendance>findClickedOutToday(@Param ("today") LocalDate today);

	List<Attendance> findByUserId(Long userId);
}