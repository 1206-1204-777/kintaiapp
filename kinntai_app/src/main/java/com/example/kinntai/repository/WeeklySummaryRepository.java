// WeeklySummaryRepository.java
package com.example.kinntai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.WeeklySummary;

@Repository
public interface WeeklySummaryRepository extends JpaRepository<WeeklySummary, Long> {
    
    // 特定ユーザーの特定年月の週次集計を取得
    List<WeeklySummary> findByUserIdAndYearAndMonthOrderByWeekNumber(Long userId, int year, int month);
    
    // 特定ユーザーの年間の週次集計を取得
    List<WeeklySummary> findByUserIdAndYearOrderByMonthAscWeekNumberAsc(Long userId, int year);
}