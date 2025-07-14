package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.kinntai.dto.ScheduleRequestDto;
import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.entity.Schedule;
import com.example.kinntai.entity.WorkType;
import com.example.kinntai.repository.ScheduleRepository;
import com.example.kinntai.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Override
    public void saveSchedule(ScheduleRequestDto request) {
        List<Schedule> schedules = new ArrayList<>();
        for (ScheduleRequestDto.ScheduleDayDto day : request.getDays()) {
            Schedule schedule = new Schedule();
            schedule.setUserId(request.getUserId());
            schedule.setDate(day.getDate());
            schedule.setType(WorkType.valueOf(day.getType())); // 例: "WORK"
            schedule.setStatus(RequestStatus.PENDING);
            schedules.add(schedule);
        }
        scheduleRepository.saveAll(schedules);
    }

    @Override
    public List<Schedule> getWeeklySchedule(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return scheduleRepository.findByUserIdAndDateBetween(userId, weekStart, weekEnd);
    }

    @Override
    public void approveSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Schedule not found"));
        schedule.setStatus(RequestStatus.APPROVED);
        scheduleRepository.save(schedule);
    }
}
