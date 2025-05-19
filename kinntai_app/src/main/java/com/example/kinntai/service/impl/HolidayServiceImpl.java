package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.kinntai.entity.Holiday;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.HolidayRepository;
import com.example.kinntai.service.HolidayService;

public class HolidayServiceImpl implements HolidayService{

	@Autowired
	private HolidayRepository holidayRepository;
	
	
	@Override
	public Holiday registerHoliday(Holiday holiday) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public List<Holiday> getUsersHoliday(User user, LocalDate startDate, LocalDate endDate) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public boolean checkUsersHoliday(User user, LocalDate date) {
		return holidayRepository.existsByUserAndDate(user, date);
	}

}
