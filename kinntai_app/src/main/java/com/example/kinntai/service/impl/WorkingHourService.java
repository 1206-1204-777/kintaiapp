package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.kinntai.entity.User;
import com.example.kinntai.entity.WorkingHour;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.repository.WorkingHourRepository;

@Service
public class WorkingHourService {

    @Autowired
    private WorkingHourRepository workingHourRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 指定ユーザーに対して定時設定を登録
     */
    public WorkingHour registerWorkingHour(Long userId, LocalTime startTime, LocalTime endTime, LocalDate effectiveFrom) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("指定されたユーザーが存在しません");
        }

        WorkingHour workingHour = new WorkingHour();
        //workingHour.setUser(optionalUser.get());
        workingHour.setWorkStartTime(startTime);	
        workingHour.setWorkEndTime(endTime);
        workingHour.setEffectiveFrom(effectiveFrom);
        workingHour.setCreatedAt(LocalDateTime.now());

        return workingHourRepository.save(workingHour);
    }

    /**
     * 最新の定時情報を取得（指定日より前で最も新しいもの）
     */
    public Optional<WorkingHour> getLatestWorkingHour(Long userId, LocalDate date) {
        return workingHourRepository
            .findTopByUserIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(userId, date);
    }
}
