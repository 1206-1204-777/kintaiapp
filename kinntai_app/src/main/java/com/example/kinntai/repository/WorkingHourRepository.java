package com.example.kinntai.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kinntai.entity.WorkingHour;

public interface WorkingHourRepository extends JpaRepository<WorkingHour, Long> {
    Optional<WorkingHour> findTopByUserIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(Long userId, LocalDate date);
}
