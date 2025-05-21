package com.example.kinntai.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.Break;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.BreakRepository;
import com.example.kinntai.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BreakServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AttendanceRepository attendanceRepository;

	@Mock
	private BreakRepository repository;

	@InjectMocks
	private BreakServiceImpl service;

	/*仮のユーザーと勤怠情報*/
	private User user;
	private Attendance attendance;
	private Break ongoingBreak;

	/*テスト用の時刻*/
	private LocalDateTime clockInTime;
	private LocalDateTime startBreak;
	private LocalDateTime endBreak;
	private Break testBreak1;

	@BeforeEach
	void sesUp() {
		testBreak1 = new Break();
		clockInTime = LocalDateTime.of(2025, 5, 10, 9, 0, 0);
		startBreak = LocalDateTime.of(2025, 5, 10, 12, 0, 0);

		user = new User();
		user.setId(1L);

		attendance = new Attendance();

		attendance.setId(100L);
		attendance.setUser(user);
		attendance.setDate(LocalDate.of(2025, 5, 10));
		attendance.setClockIn(clockInTime);
		attendance.setClockOut(null);

		ongoingBreak = new Break();
		ongoingBreak.setId(200L);
		ongoingBreak.setAttendance(attendance);
		ongoingBreak.setBreakStart(startBreak.minusHours(1));
		ongoingBreak.setBreakEnd(null);//終了したかをこの後テストするためにnullにする
		ongoingBreak.setDurationMinutes(null);

	}

	@Test
	@DisplayName("休憩開始が正常に記録されること")
	void breakstart_true() {
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(attendanceRepository.findByUserAndDate(user, LocalDate.now())).thenReturn(Optional.of(attendance));
		when(repository.findByAttendanceAndBreakEndIsNull(attendance)).thenReturn(Optional.empty());//進行中の休憩は無しの設定

		when(repository.save(any(Break.class))).thenAnswer(invocation -> {
			Break seved = invocation.getArgument(0);
			seved.setId(300L);
			return seved;
		});

		Break result = service.startBreak(user.getId());

		assertNotNull(ongoingBreak);
		assertEquals(attendance.getId(), result.getAttendance().getId());
		assertNull(result.getBreakEnd());

		verify(userRepository, times(1)).findById(eq(user.getId()));
		verify(attendanceRepository, times(1)).findByUserAndDate(eq(user), eq(LocalDate.now()));
		verify(repository, times(1)).findByAttendanceAndBreakEndIsNull(eq(attendance));
		verify(repository, times(1)).save(any(Break.class)); // 引数にany(Break.class)を使う

	}

	@Test
	@DisplayName("休憩終了後に時間の計算をする")
	void breakend_true() {
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(attendanceRepository.findByUserAndDate(user, LocalDate.now())).thenReturn(Optional.of(attendance));
		when(repository.findByAttendanceAndBreakEndIsNull(attendance)).thenReturn(Optional.of(ongoingBreak));

		when(repository.save(any(Break.class))).thenAnswer(invocation -> {
			Break seved = invocation.getArgument(0);
			seved.setId(200L);
			return seved;
		});

		Break result = service.endBreak(user.getId());

		assertNotNull(ongoingBreak);
		assertEquals(attendance.getId(), result.getAttendance().getId());
		assertNotNull(result.getBreakEnd());
		assertNotNull(result.getBreakEnd());

		verify(userRepository, times(1)).findById(eq(user.getId()));
		verify(attendanceRepository, times(1)).findByUserAndDate(eq(user), eq(LocalDate.now()));
		verify(repository, times(1)).findByAttendanceAndBreakEndIsNull(eq(attendance));
		verify(repository, times(1)).save(any(Break.class)); // 引数にany(Break.class)を使う

	}
}
