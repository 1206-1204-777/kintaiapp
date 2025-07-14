package com.example.kinntai.service;

import java.time.LocalDate;
import java.util.List;

import com.example.kinntai.dto.ScheduleRequestDto;
import com.example.kinntai.entity.Schedule;

public interface ScheduleService {
    void saveSchedule(ScheduleRequestDto request);
    List<Schedule> getWeeklySchedule(Long userId, LocalDate weekStart);
	void approveSchedule(Long id);
}
