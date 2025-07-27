package com.example.kinntai.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.entity.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<Schedule> findByUserId(Long userId);
    void deleteByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<Schedule> findByStatus(RequestStatus status);
    int countByStatus(RequestStatus status);
    List<Schedule> findAllByOrderByDateDesc();
}