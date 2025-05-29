package com.example.kinntai.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class CorrectionRequestDto {
	private LocalDate date;
	private String reason;
	private LocalTime startTime;
	private LocalTime endTime;
	private String message;

}
