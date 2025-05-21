package com.example.kinntai.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.Break;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.BreakRepository;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.BreakService;

@Service
public class BreakServiceImpl implements BreakService {

	@Autowired
	private BreakRepository breakRepository;

	@Autowired
	private UserRepository users;

	@Autowired
	private AttendanceRepository attendances;

	@Transactional
	@Override
	public Break startBreak(Long userId) {

		/*ユーザーの存在確認*/
		User user = users.findById(userId)
				.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません。"));

		/*当日の勤怠があるか確認*/
		Attendance attendance = attendances.findByUserAndDate(user, LocalDate.now())
				.orElseThrow(() -> new RuntimeException("勤怠情報がありません。"));

		/*出勤しているかの確認*/
		if (attendance.getClockIn() == null) {
			throw new RuntimeException("本日は出勤していません。");

		}

		Optional<Break> ongoingBreak = breakRepository.findByAttendanceAndBreakEndIsNull(attendance);
		if (ongoingBreak.isPresent()) {
			throw new RuntimeException("すでに開始した休憩記録があります。");

		}

		Break newBreak = new Break();
		newBreak.setBreakStart(LocalDateTime.now());
		newBreak.setAttendance(attendance);
		//休憩終了と経過時間はendBreakで入れて計算する
		return breakRepository.save(newBreak);
	}

	@Transactional
	@Override
	public Break endBreak(Long userId) {

		/*ユーザーの存在確認*/
		User user = users.findById(userId)
				.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません。"));

		/*当日の勤怠があるか確認*/
		Attendance attendance = attendances.findByUserAndDate(user, LocalDate.now())
				.orElseThrow(() -> new RuntimeException("勤怠情報がありません。"));

		/*出勤しているかの確認*/
		if (attendance.getClockIn() == null) {
			throw new RuntimeException("本日は出勤していません。");

		}

		Break ongoingBreak = breakRepository.findByAttendanceAndBreakEndIsNull(attendance)
				.orElseThrow(() -> new RuntimeException("休憩開始の時間がありません。"));

		/*休憩終了時間を設定*/
		ongoingBreak.setBreakEnd(LocalDateTime.now());

		/*休憩時間の計算*/
		Duration duration = Duration.between(ongoingBreak.getBreakStart(), ongoingBreak.getBreakEnd());
		ongoingBreak.setDurationMinutes(duration.toMinutes());//分単位で保存
		return breakRepository.save(ongoingBreak);
	}

	@Override
	public boolean isOngoingBreakToday(Long userId) {
		User user = users.findById(userId)
				.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません。"));

		Optional<Attendance> optionalAttendance = attendances.findByUserAndDate(user, LocalDate.now());
		if (optionalAttendance.isEmpty()) {

		}
		Attendance attendance = optionalAttendance.get();

		return breakRepository.findByAttendanceAndBreakEndIsNull(attendance).isPresent();

	}

	@Override
	public Long getTotalBreakMinutesForToday(Long userId) {
		User user = users.findById(userId)
				.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません。"));

		Optional<Attendance> optionalAttendance = attendances.findByUserAndDate(user, LocalDate.now());
		if (optionalAttendance.isEmpty()) {
			return 0L;
		}

		Attendance attendance = optionalAttendance.get();

		return breakRepository.findByAttendanceAndBreakEndIsNotNull(attendance)
				.stream()
				.mapToLong(Break::getDurationMinutes)
				.sum();

	}

}
