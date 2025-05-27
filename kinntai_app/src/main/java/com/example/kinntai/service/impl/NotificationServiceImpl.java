package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // 追加
import java.util.List;

import org.slf4j.Logger; // 変更
import org.slf4j.LoggerFactory; // 変更
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException; // 追加
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async; // 追加
import org.springframework.stereotype.Service;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

	private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class); // クラス名を変更

	@Autowired
	private UserRepository userRepository; // 'repository' から 'userRepository' に変更（可読性のため）

	@Autowired
	private JavaMailSender mailSender;
	
    // `@Async` を有効にするには、アプリケーションのメインクラスに `@EnableAsync` が必要です。

	@Override
	@Async // 未退勤アラートメール送信全体を非同期で実行
	public void sendUnclockedOutAlert(List<Attendance> unclocked) {
		for (Attendance a : unclocked) {
			User user = a.getUser();
			
            // ユーザーのメールアドレスが有効かチェック
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                sendToUser(user, a.getDate()); // ユーザー本人にのみ送信
            } else {
                logger.warn("ユーザー '{}' ({}) のメールアドレスが登録されていないか、無効です。未退勤アラートメールを送信できません。", user.getUsername(), user.getId());
            }
		}

		// 管理者への通知は今回の要件では削除
		// List<User> admins = userRepository.findByRole(UserRole.ADMIN);
		// String summary = createSummary(unclocked);
		// for (User admin : admins) {
		// 	sendToAdmin(admin.getEmail(), summary);
		// }
	}

	/* ユーザー送信用 */
	@Override
	@Async // 個別のメール送信も非同期で実行（二重になるが問題ない）
	public void sendToUser(User user, LocalDate date) {
        String subject = "【自動通知】退勤処理が完了していません - " + user.getUsername();
        String body = String.format(
            "お疲れ様です、%sさん。\n\n" +
            "本日 %s の退勤処理がまだ完了していないようです。\n" +
            "お忘れの場合は、速やかに勤怠システムで退勤処理を行ってください。\n\n" +
            "ご不明な点があれば、管理者にお問い合わせください。\n\n" +
            "----------------------------------------\n" +
            "このメールはシステムからの自動通知です。",
            user.getUsername(),
            date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) // 日付のフォーマット
        );

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(user.getEmail());
		message.setSubject(subject);
		message.setText(body);

        try {
            mailSender.send(message);
            logger.info("未退勤アラートメールをユーザー {} ({}) に送信しました。", user.getUsername(), user.getEmail()); // ログ追加
        } catch (MailException e) {
            logger.error("未退勤アラートメールの送信に失敗しました（宛先: {}）: {}", user.getEmail(), e.getMessage()); // エラーログ追加
        }
	}

	/* 管理者宛てメール送信（今回の要件では不要なため、削除またはコメントアウト） */
	@Override
	public void sendToAdmin(String email, String summary) {
        // このメソッドは今回の要件では使用しないため、削除またはコメントアウトすることを推奨します。
        // もし残す場合は、@Async を付与し、エラーハンドリングを追加することを推奨します。
        logger.info("管理者への打刻漏れサマリーメールは送信されません。（要件外）");
	}

	/* 退勤してないユーザーをまとめる（sendToAdmin が不要になるため、このメソッドも不要になります） */
	@Override
	public String createSummary(List<Attendance> unclocked) {
        // このメソッドは sendToAdmin が不要になるため、今回は使用しません。
        // 必要であれば残しておいても構いませんが、未使用コードとして扱われます。
        logger.info("createSummary メソッドは使用されません。（要件外）");
		return ""; // 使用しないため、空文字を返す
	}

}