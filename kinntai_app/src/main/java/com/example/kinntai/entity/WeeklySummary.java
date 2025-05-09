package com.example.kinntai.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "weekly_summary")
public class WeeklySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "year")
    private Integer year;
    
    @Column(name = "month")
    private Integer month;
    
    @Column(name = "week_number")
    private Integer weekNumber;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "work_days")
    private Integer workDays;
    
    @Column(name = "total_work_hours")
    private Double totalWorkHours;
    
    @Column(name = "average_work_hours")
    private Double averageWorkHours;
    
    @Column(name = "overtime_hours")
    private Double overtimeHours;
}