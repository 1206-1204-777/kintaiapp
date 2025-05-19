package com.example.kinntai.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kinntai.entity.User;
import com.example.kinntai.entity.UserRole;
import com.example.kinntai.repository.HolidayRepository;

@ExtendWith(MockitoExtension.class)
class HolidayServiceImplTest {

	@Mock
	private HolidayRepository repository;

	@InjectMocks
	private HolidayServiceImpl service;

	private LocalDate testDate;
	private User user;

	@BeforeEach
	void setup() {
		user = new User();
		testDate = LocalDate.of(2025, 5,15);
		user.setId(1L);
		user.setRole(UserRole.USER);

	}

	@Test
	void 休日の取得_成功() {
		when(repository.existsByUserAndDate(user, testDate)).thenReturn(true);
		
		/*サービスを動かす*/
		boolean isHoliday = service.checkUsersHoliday(user, testDate);
		
		/*検証*/
		assertTrue(isHoliday);
		
		/*Repositoryが1度だけ動いたかを検証
		 * 何度も動いていると重複や上書きの可能性がある為*/
		verify(repository,times(1)).existsByUserAndDate(user, testDate);
	}
	

}
