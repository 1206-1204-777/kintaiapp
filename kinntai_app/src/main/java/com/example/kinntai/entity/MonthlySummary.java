package com.example.kinntai.entity;

import java.time.YearMonth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "monthly_summary")
public class MonthlySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "year")
    private Integer year;
    
    @Column(name = "month")
    private Integer month;
    
    @Column(name = "work_days")
    private Integer workDays;
    
    @Column(name = "total_work_hours")
    private Double totalWorkHours;
    
    @Column(name = "average_work_hours")
    private Double averageWorkHours;
    
    @Column(name = "overtime_hours")
    private Double overtimeHours;
    
    @Column(name = "absent_days")
    private Integer absentDays;
    
    @Column(name = "holiday_count")
    private Integer holidayCount;
    
    // YearMonthを取得するメソッド（JPA外で使用）
    public YearMonth getYearMonth() {
        return YearMonth.of(year, month);
    }
    
    // YearMonthを設定するメソッド（JPA外で使用）
    public void setYearMonth(YearMonth yearMonth) {
        this.year = yearMonth.getYear();
        this.month = yearMonth.getMonthValue();
    }
}