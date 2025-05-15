package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.UserRole;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.NotificationService;

@Service

public class NotificationServiceImpl implements NotificationService {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(NotificationService.class);
	@Autowired
	private UserRepository repository;

	@Autowired
	private JavaMailSender mailSender;
	/*メール送信用*/

	@Override
	public void sendUnclockedOutAlert(List<Attendance> unclocked) {

		for (Attendance a : unclocked) {
			User user = a.getUser();
			if (user.getRole() == UserRole.USER) {
				User response = new User();
				response.setEmail(user.getEmail());
				response.setUsername(user.getUsername());
				sendToUser(response, a.getDate());
			}
		}

		List<User> admins = repository.findByRole(UserRole.ADMIN);
		String summary = createSummary(unclocked);
		for (User admin : admins) {
			sendToAdmin(admin.getEmail(), summary);
		}
	}

	/*ユーザー送信用*/
	@Override
	public void sendToUser(User user, LocalDate date) {
		String subject = "退勤打刻漏れのお知らせ";
		String body = String.format("%sさん、%s の退勤打刻が漏れています。ご確認ください。",
				user.getUsername(), date.toString());

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(user.getEmail());
		message.setSubject(subject);
		message.setText(body);

		mailSender.send(message);
	}

	/*管理者宛てメール送信*/
	@Override
	public void sendToAdmin(String user, String summary) {
		String subject = "打刻漏れメンバー通知";
		//User users = new User();

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(user);
		message.setSubject(subject);
		message.setText(summary);

		mailSender.send(message);

	}

	/*退勤してないユーザーをまとめる*/
	@Override
	public String createSummary(List<Attendance> unclocked) {

		StringBuilder summary = new StringBuilder();

		for (Attendance a : unclocked) {

			summary.append(String.format("- %s（%s）\n",
					a.getUser().getUsername(), a.getDate().toString()));
		}

		log.info("summary：" + summary);
		return summary.toString();
	}

}
