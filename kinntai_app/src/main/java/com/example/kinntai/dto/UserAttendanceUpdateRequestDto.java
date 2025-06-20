package com.example.kinntai.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.kinntai.entity.User;

import lombok.Data;

@Data
public class UserAttendanceUpdateRequestDto {

	private User userId;
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private String comment;
	private String reason;
	
	//定数を文字列に変換

}
