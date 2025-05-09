package com.example.kinntai.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class HolidayDto {
	private String name;
	private LocalDate date;
}
