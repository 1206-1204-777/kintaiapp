package com.example.kinntai.controller;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.entity.WorkingHour;
import com.example.kinntai.service.impl.WorkingHourService;

@RestController
@RequestMapping("/api/working-hours")
public class WorkingHourController {

    @Autowired
    private WorkingHourService workingHourService;

    @PostMapping("/{userId}")
    public WorkingHour registerWorkingHour(
            @PathVariable Long userId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String effectiveFrom) {

        return workingHourService.registerWorkingHour(
            userId,
            LocalTime.parse(startTime),
            LocalTime.parse(endTime),
            LocalDate.parse(effectiveFrom));
    }

  
}
