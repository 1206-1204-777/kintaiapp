package com.example.kinntai.service;

import java.time.LocalDate;
import java.util.List;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.User;

public interface NotificationService {
	//public void sendUnclockedOutAlert(User user);
	
	public void sendToUser(User user,LocalDate date);
	
	public void sendUnclockedOutAlert(List<Attendance> unclocked);
	
	public void sendToAdmin(String user,String summary);
	
	public String createSummary(List<Attendance> unclocked);
}
