package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
		List<Attendance> dailyAttendances = attendanceRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

		//ファイル形式にごとに出力する	
		if ("excel".equalsIgnoreCase(format)) {
			//TODO: ここでExcelを出力する
		}
		if ("csv".equalsIgnoreCase(format)) {
			//TODO: ここでcsvを出力する
		} else {
			//どの形式でもない場合は400を返す
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok().build();
	}

}
