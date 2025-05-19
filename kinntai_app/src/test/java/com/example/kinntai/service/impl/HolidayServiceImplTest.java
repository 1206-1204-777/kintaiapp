package com.example.kinntai.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

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
	}

	@Test
	@DisplayName("休日登録が成功")
	void registerHoliday_true() {
		when(repository.save(newHoliday)).thenReturn(savedHoliday);

		Holiday registerHoliday = service.registerHoliday(newHoliday);
		assertNotNull(registerHoliday); //休日情報がnullになってないかをチェック
		assertEquals(registerHoliday, savedHoliday);

		assertNotNull(registerHoliday.getUser()); //ユーザー情報が紐づいているかをチェック
		assertEquals(user.getId(),registerHoliday.getUser().getId());
		
		verify(repository,times(1)).save(eq(newHoliday));
	}
	
	@Test
	@DisplayName("登録時の例外テスト")
	void registerHoliday_exception() {
		when(service.registerHoliday(newHoliday)).thenThrow(new DataAccessException("登録に失敗しました。") {});
		
		assertThrows(DataAccessException.class,() ->{
			service.registerHoliday(newHoliday);
		});
		
		verify(repository,times(1)).save(eq(newHoliday));
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

		verify(repository, times(1)).existsByUserAndDate(user, testDate);
	}

}
