package com.example.kinntai.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.service.impl.AttendanceService;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @PostMapping("/clock-in/{userId}")
    public Attendance clockIn(@PathVariable Long userId) {
        return attendanceService.clockIn(userId);
    }
    
    @PostMapping("/clock-out/{userId}")
    public Attendance clockOut(@PathVariable Long userId) {
        return attendanceService.clockOut(userId);
    }
    
    @GetMapping("/monthly/{userId}")
    public List<Attendance> getMonthlyAttendances(@PathVariable Long userId, @RequestParam String month) {
        return attendanceService.getMonthlyAttendances(userId, month);
    }
    
    @GetMapping("/{userId}/date/{date}")
    public Attendance getAttendanceByDate(@PathVariable Long userId, 
                                         @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.getAttendanceByDate(userId, date);
    }
    
    @GetMapping("/{userId}/status")
    public Map<String, Object> getWorkingStatus(@PathVariable Long userId) {
        boolean working = attendanceService.isWorking(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("working", working);
        
        return response;
    }
}