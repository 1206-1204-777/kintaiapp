package com.example.kinntai.service;

import java.time.LocalDate;
import java.util.List;

import com.example.kinntai.entity.Holiday;
import com.example.kinntai.entity.User;

public interface HolidayService {

	/*登録と更新*/
	public Holiday registerHoliday(Holiday holiday);

	/*ユーザーごとの休日をリストにして確認*/
	public List<Holiday> getUsersHoliday(User user, LocalDate startDate, LocalDate endDate);

	/*休日が登録されているかを確認*/
	public boolean checkUsersHoliday(User user, LocalDate date);
}
