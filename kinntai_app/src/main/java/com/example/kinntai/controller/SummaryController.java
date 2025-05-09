package com.example.kinntai.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.entity.MonthlySummary;
import com.example.kinntai.service.impl.SummaryService;

@RestController
@RequestMapping("/api/summary")
@CrossOrigin(origins = "*")
public class SummaryController {
    
    @Autowired
    private SummaryService summaryService;
    
    /**
     * 月次集計データを取得
     */
    @GetMapping("/{userId}/month/{month}")
    public Map<String, Object> getMonthlySummary(@PathVariable Long userId, @PathVariable String month) {
        return summaryService.getMonthlySummaryData(userId, month);
    }
    
    /**
     * 月次集計を手動で計算（管理者用）
     */
    @GetMapping("/calculate/{userId}")
    public MonthlySummary calculateMonthlySummary(@PathVariable Long userId, 
                                                 @RequestParam int year, 
                                                 @RequestParam int month) {
        return summaryService.calculateMonthlySummary(userId, year, month);
    }
    
    /**
     * 月次バッチを手動実行（管理者用）
     */
    @GetMapping("/run-monthly-batch")
    public String runMonthlyBatch() {
        summaryService.executeMonthlyBatch();
        return "月次集計バッチが実行されました";
    }
    
    /**
     * 週次バッチを手動実行（管理者用）
     */
    @GetMapping("/run-weekly-batch")
    public String runWeeklyBatch() {
        summaryService.executeWeeklyBatch();
        return "週次集計バッチが実行されました";
    }
}