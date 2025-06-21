package com.example.kinntai.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class UserAttendanceUpdateRequestDto {

	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private String comment;
	private String reason;
	

}
