package com.example.kinntai.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.WeeklySummary;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.repository.WeeklySummaryRepository;
import com.example.kinntai.service.WeeklySummalyService;

import io.jsonwebtoken.io.IOException;

/*週次記録に関するクラス*/
@Service
public class WeeklySummalyServiceImpl implements WeeklySummalyService {

	@Autowired
	private WeeklySummaryRepository summaryRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AttendanceRepository attendanceRepository;

	/*勤怠情報をExcelで出力するメソッド*/
	@Override
	public ResponseEntity<Resource> generateWeeklyReport(
			Long userId, int year, int weekNumber, String format) {

		//権限チェック
		Authentication authentication = SecurityContextHolder
				.getContext().getAuthentication();
		String currentUser = authentication.getName();
		User user = userRepository.findByUsername(currentUser)
				.orElse(null);
		
		User targetUser = userRepository.findById(userId)
				.orElseThrow(null);
		if(targetUser == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
			
		

		boolean isAdmin = authentication.getAuthorities()
				.stream()
				.anyMatch(ga -> ga.getAuthority().equals("Role_ADMIN"));

		//管理者でもな自信の勤怠情報を取得しようとした場合、アクセスを拒否
		if (!isAdmin && !user.getId().equals(userId)) {
			return new ResponseEntity<Resource>(HttpStatus.FORBIDDEN);
		}

		//勤怠情報を取得する
		Optional<WeeklySummary> summaryOpt = summaryRepository.findByUserIdAndYearAndWeekNumber(
				userId, year, weekNumber);

		//勤怠情報がない場合４０４を返す
		if (!summaryOpt.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		//
		WeeklySummary summary = summaryOpt.get();

		//集計データに基づいて、日々の勤怠情報を取得
		LocalDate startDate = summary.getStartDate();
		LocalDate endDate = summary.getEndDate();
		List<Attendance> dailyAttendances = 
				attendanceRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
		try {
			Resource resource;
			String filename = 
					"weekly_report_" + year + "_w" + weekNumber + "_" + targetUser.getUsername();
			//ファイル形式にごとに出力する	
			if ("excel".equalsIgnoreCase(format)) {

			}
			if ("csv".equalsIgnoreCase(format)) {
				//TODO: ここでcsvを出力する
			} else {
				//どの形式でもない場合は400を返す
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}return ResponseEntity.ok().build();
	}

	// ReportService.java のクラスの末尾に追加

	/**
	 * Excelレポートファイルを生成するプライベートメソッド
	 * @throws java.io.IOException 
	 */
	private Resource createExcelReport(User user, WeeklySummary summary, List<Attendance> dailyAttendances)
			throws IOException, java.io.IOException {
		// 1. テンプレートファイルを読み込む
		InputStream template = new ClassPathResource("templates/weekly_report_template.xlsx").getInputStream();
		Workbook workbook = new XSSFWorkbook(template);
		Sheet sheet = workbook.getSheetAt(0);

		// --- ヘッダー情報の書き込み ---
		// ユーザー名 (C3セル)
		sheet.getRow(2).getCell(2).setCellValue(user.getUsername());
		// 対象期間 (C4セル)
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		String period = summary.getStartDate().format(dateFormatter) + " - "
				+ summary.getEndDate().format(dateFormatter);
		sheet.getRow(3).getCell(2).setCellValue(period);
		// 作成日 (C5セル)
		sheet.getRow(4).getCell(2).setCellValue(LocalDate.now().format(dateFormatter));

		// --- 日次勤怠データの書き込み ---
		// 日付をキーにしたMapを作成しておくと、特定の日の勤怠データを簡単に取得できる
		Map<LocalDate, Attendance> attendanceMap = dailyAttendances.stream()
				.collect(Collectors.toMap(Attendance::getDate, a -> a));

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

		// 7日分ループ（テンプレートの8行目から14行目まで）
		for (int i = 0; i < 7; i++) {
			Row row = sheet.getRow(7 + i);
			LocalDate currentDate = summary.getStartDate().plusDays(i);

			// 日付と曜日を書き込む
			row.getCell(1).setCellValue(currentDate.format(dateFormatter)); // 日付
			row.getCell(2).setCellValue(currentDate.format(DateTimeFormatter.ofPattern("E", Locale.JAPANESE))); // 曜日

			Attendance attendance = attendanceMap.get(currentDate);
			if (attendance != null) {
				// その日の勤怠データが存在する場合
				if (attendance.getClockIn() != null)
					row.getCell(3).setCellValue(attendance.getClockIn().format(timeFormatter));
				if (attendance.getClockOut() != null)
					row.getCell(4).setCellValue(attendance.getClockOut().format(timeFormatter));
				row.getCell(5).setCellValue(Optional.ofNullable(attendance.getTotalWorkMin()).orElse(0L));
				row.getCell(6).setCellValue(Optional.ofNullable(attendance.getOvertimeMinutes()).orElse(0L));
				// TODO: 備考欄の処理
			}
		}

		// --- フッター情報（週次集計）の書き込み ---
		sheet.getRow(16).getCell(4).setCellValue(summary.getTotalWorkHours());
		sheet.getRow(17).getCell(4).setCellValue(summary.getOvertimeHours());
		sheet.getRow(18).getCell(4).setCellValue(summary.getWorkDays());

		// 2. 作成したExcelブックをバイト配列に変換する
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		workbook.write(out);
		workbook.close();

		return new ByteArrayResource(out.toByteArray());
	}

}
