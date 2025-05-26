package com.example.kinntai.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.PersonalHoliday;

@Repository
public interface PersonalHolidayRepository extends JpaRepository<PersonalHoliday, Long> {

    // 特定のユーザーのすべての個人休日申請を検索 (新しいものから順に)
    List<PersonalHoliday> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // 特定のユーザーの特定の日付の個人休日申請を検索
    Optional<PersonalHoliday> findByUser_IdAndHolidayDate(Long userId, LocalDate holidayDate);

    // 特定のユーザーの指定されたステータスの個人休日申請を検索
    List<PersonalHoliday> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}