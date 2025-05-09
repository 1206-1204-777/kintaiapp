// HolidayRepository.java
package com.example.kinntai.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.Holiday;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    
    // 特定日が休日かどうか確認
    boolean existsByDate(LocalDate date);
    
    // 期間内の休日を取得
    @Query("SELECT h FROM Holiday h WHERE h.date BETWEEN :startDate AND :endDate ORDER BY h.date")
    List<Holiday> findHolidaysBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // 特定年月の休日数を取得
    @Query("SELECT COUNT(h) FROM Holiday h WHERE YEAR(h.date) = :year AND MONTH(h.date) = :month")
    int countHolidaysInMonth(@Param("year") int year, @Param("month") int month);
}