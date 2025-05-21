package com.example.kinntai.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import com.example.kinntai.entity.Holiday;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.UserRole;
import com.example.kinntai.repository.HolidayRepository;

@ExtendWith(MockitoExtension.class)
class HolidayServiceImplTest {

	@Mock
	private HolidayRepository repository;

	@InjectMocks
	private HolidayServiceImpl service;

	private Holiday newHoliday;
	private Holiday savedHoliday;
	private LocalDate testDate;
	private User user;

	/*取得するメソッド用*/
	private LocalDate startDate;
	private LocalDate endDate;
	private List<Holiday> testHolidayList;

	@BeforeEach
	void setup() {
		user = new User();
		testDate = LocalDate.of(2025, 5, 15);
		user.setId(1L);
		user.setRole(UserRole.USER);

		/*受信した内容と想定*/
		newHoliday = new Holiday();
		newHoliday.setDate(testDate);
		newHoliday.setName("有給休暇");
		newHoliday.setNationalHoliday(false);
		newHoliday.setUser(user);

		/*返却する値*/
		savedHoliday = new Holiday();
		savedHoliday.setId(100L);
		savedHoliday.setDate(testDate);
		savedHoliday.setName("有給休暇");
		savedHoliday.setNationalHoliday(false);
		savedHoliday.setUser(user);

		/*リスト用*/
		startDate = LocalDate.of(2025, 5, 1);
		endDate = LocalDate.of(2025, 5, 3);

		Holiday holiday1 = new Holiday();
		holiday1.setId(100L);
		holiday1.setName("有給休暇");
		holiday1.setUser(user);
		holiday1.setDate(LocalDate.of(2025, 5, 2)); // 5/1～5/3までの期間中かを判断するため5/2に定義

		Holiday holiday2 = new Holiday();
		holiday2.setId(101L);
		holiday2.setName("代休");
		holiday2.setUser(user);
		holiday2.setDate(LocalDate.of(2025, 5, 10));

		testHolidayList = Arrays.asList(holiday1, holiday2);

	}

	@Test
	@DisplayName("休日登録が成功")
	void registerHoliday_true() {
		when(repository.save(newHoliday)).thenReturn(savedHoliday);

		Holiday registerHoliday = service.registerHoliday(newHoliday);
		assertNotNull(registerHoliday); //休日情報がnullになってないかをチェック
		assertEquals(registerHoliday, savedHoliday);

		assertNotNull(registerHoliday.getUser()); //ユーザー情報が紐づいているかをチェック
		assertEquals(user.getId(), registerHoliday.getUser().getId());

		verify(repository, times(1)).save(eq(newHoliday));
	}

	@Test
	@DisplayName("登録時の例外テスト")
	void registerHoliday_exception() {
		when(repository.save(newHoliday)).thenThrow(new DataAccessException("登録に失敗しました。") {
		});

		assertThrows(DataAccessException.class, () -> {
			service.registerHoliday(newHoliday);
		});

		verify(repository, times(1)).save(eq(newHoliday));
	}

	@Test
	@DisplayName("休日取得に成功")
	void isHoliday_True() {
		when(repository.existsByUserAndDate(user, testDate)).thenReturn(true);

		/*サービスを動かす*/
		boolean isHoliday = service.checkUsersHoliday(user, testDate);

		/*検証*/
		assertTrue(isHoliday);

		/*Repositoryが1度だけ動いたかを検証
		 * 何度も動いていると重複や上書きの可能性がある為*/
		verify(repository, times(1)).existsByUserAndDate(user, testDate);
	}

	@Test
	@DisplayName("休日取得に失敗")
	void isHoliday_false() {
		when(repository.existsByUserAndDate(user, testDate)).thenReturn(false);
		boolean isHoliday = service.checkUsersHoliday(user, testDate);
		assertFalse(isHoliday);

		verify(repository, times(1)).existsByUserAndDate(user, testDate);
	}

	@Test
	@DisplayName("リポジトリ呼び出し時の例外テスト")
	void Exception() {

		when(repository.existsByUserAndDate(user, testDate))
				.thenThrow(new RuntimeException("リポジトリから値が取得できませんでした。"));

		assertThrows(RuntimeException.class, () -> {
			service.checkUsersHoliday(user, testDate);
		});

		verify(repository, times(1)).existsByUserAndDate(eq(user), eq(testDate));
	}

	@Test
	@DisplayName("休日取得に成功")
	void getUserHoliday_true() {

		when(repository.findByUserAndDateBetweenOrderByDateAsc(
				user, startDate, endDate)).thenReturn(testHolidayList);

		List<Holiday> getUserHolidayList = service.getUsersHoliday(user, startDate, endDate);
		assertNotNull(getUserHolidayList);
		assertEquals(getUserHolidayList, testHolidayList);

		verify(repository, times(1)).findByUserAndDateBetweenOrderByDateAsc(eq(user), eq(startDate), eq(endDate));

	}

	@Test
	@DisplayName("休日取得に失敗")
	void getUserHoliday_false() {
		when(repository.findByUserAndDateBetweenOrderByDateAsc(user, startDate, endDate))
				.thenReturn(Collections.emptyList()); //失敗時はからのリストを返す
		List<Holiday> holidayList = service.getUsersHoliday(user, startDate, endDate);
		assertNotNull(holidayList);
		assertTrue(holidayList.isEmpty());
		verify(repository, times(1)).findByUserAndDateBetweenOrderByDateAsc(eq(user), eq(startDate), eq(endDate));
	}

	@Test
	@DisplayName("休日取得時の例外")
	void holidayException() {
		when(repository.findByUserAndDateBetweenOrderByDateAsc(user, startDate, endDate))
				.thenThrow(new DataAccessException("休日を取得できませんでした。") {
				});

		assertThrows(DataAccessException.class, () -> {
			service.getUsersHoliday(user, startDate, endDate);
		});

		verify(repository, times(1)).findByUserAndDateBetweenOrderByDateAsc(eq(user), eq(startDate), eq(endDate));

	}

}
