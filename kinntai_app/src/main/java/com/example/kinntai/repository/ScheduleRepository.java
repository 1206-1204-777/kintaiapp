package com.example.kinntai.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<Schedule> findByUserId(Long userId); // userIdで全てのスケジュールを取得するメソッドを追加

    // 新規追加: 指定されたユーザーと日付範囲のスケジュールを削除するメソッド
    void deleteByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
