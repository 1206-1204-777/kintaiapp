package com.example.kinntai.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface WeeklySummalyService {
ResponseEntity<Resource> generateWeeklyReport(
		Long userId,int year,int weekNumber,String format);
}
