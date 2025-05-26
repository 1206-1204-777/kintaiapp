package com.example.kinntai.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.CompanyHoliday;

@Repository
public interface CompanyHolidayRepository extends JpaRepository<CompanyHoliday, Long> {

    // すべての会社休日を日付順に検索 (古いものから新しいものへ)
    List<CompanyHoliday> findAllByOrderByHolidayDateAsc();

    // 特定の日付の会社休日を検索
    Optional<CompanyHoliday> findByHolidayDate(LocalDate holidayDate);
}