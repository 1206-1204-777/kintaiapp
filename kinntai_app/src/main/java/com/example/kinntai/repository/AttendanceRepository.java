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

    Optional<Attendance> findByUser_IdAndDate(Long userId, LocalDate date);

	List<Attendance> findByUser_IdAndDateBetweenOrderByDateAsc(Long userId, LocalDate startDate, LocalDate endDate);

	@Query("SELECT a FROM Attendance a WHERE a.user.id = ?1 AND YEAR(a.date) = ?2 AND MONTH(a.date) = ?3 ORDER BY a.date ASC")
	List<Attendance> findByUserIdAndYearAndMonth(Long userId, int year, int month);

	List<Attendance> findByDateBetween(LocalDate startDate, LocalDate endDate);

	/*退勤していないユーザーを見つける*/
	@Query("SELECT a FROM Attendance a WHERE a.date = :today AND a.clockIn IS NOT NULL AND a.clockOut IS NULL")
	List<Attendance> findClickedOutToday(@Param("today") LocalDate today);

	List<Attendance> findByUserId(Long userId);

	@Query("SELECT a FROM Attendance a WHERE a.date = :date AND a.clockIn IS NOT NULL AND a.clockOut IS NULL")
	List<Attendance> findByAttendanceDateAndClockInNotNullAndClockOutIsNull(@Param("date") LocalDate today);
	
    List<Attendance> findAllByUser_Id(Long userId);
    List<Attendance> findAllByUser_IdAndDate(Long userId, LocalDate date);}