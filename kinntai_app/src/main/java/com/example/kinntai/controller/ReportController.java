package com.example.kinntai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.service.WeeklySummalyService;
/*勤怠情報をダウンロードするAPI*/
@RestController
@RequestMapping("/api/export")
public class ReportController {
	@Autowired
	private WeeklySummalyService service;
	
	@GetMapping("/personal/{userId}")
	public ResponseEntity<Resource> downloadWeeklyReport(
			@PathVariable Long userId,
			@RequestParam int year,
			@RequestParam int weeknum,
			@RequestParam String format) {
		return service.generateWeeklyReport(userId, year, weeknum, format);

	}
}
