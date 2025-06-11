package com.example.kinntai.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*勤怠情報をダウンロードするAPIエンドポイント
 * */
@RestController
@RequestMapping("/api/reports")
public class ReportController {
	public ResponseEntity<Resource> downloadWeeklyReport(
			@RequestParam Long userId,
			@RequestParam int year,
			@RequestParam int weeknum,
			@RequestParam String format) {
		return ResponseEntity.ok().build();

	}
}
