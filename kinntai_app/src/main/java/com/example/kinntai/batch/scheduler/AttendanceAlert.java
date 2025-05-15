package com.example.kinntai.batch.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.service.NotificationService;

@Service
public class AttendanceAlert {
	@Autowired
	private AttendanceRepository attendanceRepository;
	@Autowired
	private NotificationService service;

	@Scheduled(cron = "0 0 21 * * *")
	@Scheduled(initialDelay = 10000, fixedRate = Long.MAX_VALUE) // 起動後10秒後に1回だけ実行
	public void notifyUnclickedOutUsers() {
		LocalDate today = LocalDate.now();

		List<Attendance> targets = attendanceRepository.findClicksedOutToday(today);
		//List <Attendance> a =  
			//User user = a.getUser();
			service.sendUnclockedOutAlert(targets);
		
	}
}
