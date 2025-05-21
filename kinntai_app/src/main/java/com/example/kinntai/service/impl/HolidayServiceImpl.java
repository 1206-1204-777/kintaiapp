package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.kinntai.entity.Holiday;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.HolidayRepository;
import com.example.kinntai.service.HolidayService;

@Service
public class HolidayServiceImpl implements HolidayService {

	@Autowired
	private HolidayRepository holidayRepository;

	@Override
	@Transactional
	public Holiday registerHoliday(Holiday holiday) {
		Holiday registerHoliday = holidayRepository.save(holiday);
		return registerHoliday;
	}

	@Override
	@Transactional
	public List<Holiday> getUsersHoliday(User user, LocalDate startDate, LocalDate endDate) {
		List<Holiday> holidays = holidayRepository.findByUserAndDateBetweenOrderByDateAsc(user, startDate, endDate);
		
		return holidays;
	}

	@Override
	@Transactional
	public boolean checkUsersHoliday(User user, LocalDate date) {
		return holidayRepository.existsByUserAndDate(user, date);
	}

}
