package com.example.kinntai.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.kinntai.entity.User;

import lombok.Data;

/**
 勤怠時間の修正申請のリクエストを受け取る*/
@Data
public class CorrectionRequestDto {

	//修正対象の勤務日
	private LocalDate targetDate;

	//申請ユーザーid
	private User requestUserId;

	//申請対象の勤務開始時間
	private LocalTime startTime;

	//申請対象の勤務終了時間
	private LocalTime endTime;
	
	//ユーザーからのコメント
	private String comment;
	
	//申請理由
	private String reason;
}
