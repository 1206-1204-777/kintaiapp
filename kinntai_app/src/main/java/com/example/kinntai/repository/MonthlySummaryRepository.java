// MonthlySummaryRepository.java
package com.example.kinntai.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.MonthlySummary;

@Repository
public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Long> {
    
    // 特定ユーザーの特定年月の集計を取得
    Optional<MonthlySummary> findByUserIdAndYearAndMonth(Long userId, int year, int month);
    
    // 特定ユーザーの年間集計を取得
    List<MonthlySummary> findByUserIdAndYearOrderByMonth(Long userId, int year);
    
    // 特定ユーザーの全集計を取得（日付順）
    List<MonthlySummary> findByUserIdOrderByYearDescMonthDesc(Long userId);
}