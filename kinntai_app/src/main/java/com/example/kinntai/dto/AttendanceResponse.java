package com.example.kinntai.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.kinntai.entity.Attendance;

import lombok.Data;

@Data
public class AttendanceResponse {
	private Long id;
	private String name;
	private Long userId;
	private String email;
	private LocalDate date;
	private LocalDateTime clockIn;
	private LocalDateTime clockOut;
	private boolean isWorking;

	public static AttendanceResponse fromEntity(Attendance entity) {

		AttendanceResponse dto = new AttendanceResponse();
		dto.setClockIn(entity.getClockIn());
		dto.setClockOut(entity.getClockOut());
		dto.setDate(entity.getDate());
		dto.setName(entity.getUser().getUsername());
		dto.setUserId(entity.getUser().getId());
		return dto;

	}
}