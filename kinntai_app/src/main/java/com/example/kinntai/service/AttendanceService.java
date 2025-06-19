package com.example.kinntai.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.kinntai.dto.AttendanceResponse;
import com.example.kinntai.dto.CorrectionRequestDto;
import com.example.kinntai.dto.UserAttendanceUpdateRequestDto;
import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.AttendanceCorrectionRequest;

public interface AttendanceService {

	/**
	 * 出勤処理
	 */
	Attendance clockIn(Long userId);

	/**
	 * 退勤処理
	 */
	Attendance clockOut(Long userId);

	/**
	 * 現在の勤務状況を取得
	 */
	AttendanceResponse getAttendanceStatus(Long userId);

	//退勤していないユーザーのリスト作成
	List<AttendanceResponse> getUnclockedUsersToday() throws RuntimeException;

	/**
	 * 特定の日の勤怠情報を取得
	 */
	Optional<Attendance> getAttendanceByDate(Long userId, LocalDate date);

	/**
	 * 月次の勤怠情報を取得
	 */
	List<Attendance> getMonthlyAttendance(Long userId, int year, int month);

	/**
	 * 月次の勤怠情報を取得（文字列指定）
	 */
	List<Attendance> getMonthlyAttendance(Long userId, String yearMonth);

	/**
	 * 期間内の勤怠情報を月曜～日曜の週で生成（存在しない日も含める）
	 */
	List<Attendance> generateWeeklyAttendances(Long userId, LocalDate startDate, LocalDate endDate);

	/*ユーザー情報の取得*/
	List<AttendanceResponse> getAllUser();

	List<AttendanceResponse> getAttendanceUser(Long userId);
	
	//勤怠時間の修正
	Attendance updateUserAttendance(Long userId,UserAttendanceUpdateRequestDto request) throws IllegalAccessException;
	
	// 勤怠時間の修正申請用メソッド
	AttendanceCorrectionRequest correctionRequest(Long userId,CorrectionRequestDto dto);

}