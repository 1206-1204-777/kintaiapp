package com.example.kinntai.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleRequestDto {
	private Long userId;
	private List<ScheduleDayDto> days;

	@Getter
	@Setter
	public static class ScheduleDayDto {
		private LocalDate date;
		private String type; // "WORK", "REMOTE", "HOLIDAY"
	}
}
