package com.example.kinntai.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@Table(name = "break")
@EqualsAndHashCode(exclude = "attendance")
public class Break {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "attendance_id", nullable = false)

	private Attendance attendance;

	@Column(name = "break_start", nullable = false)
	private LocalDateTime breakStart;
	
	@Column(name = "break_end")
	private LocalDateTime breakEnd;
	
	@Column(name = "duration_minutes")
	private Long durationMinutes; // 休憩時間の計算結果
}
