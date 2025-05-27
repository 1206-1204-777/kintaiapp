package com.example.kinntai.batch.scheduler;

import java.time.LocalDate;
import java.time.LocalTime; // 追加
import java.util.List;

import org.slf4j.Logger; // 追加
import org.slf4j.LoggerFactory; // 追加
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled; // 追加
import org.springframework.stereotype.Component; // ServiceではなくComponentに推奨

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.service.NotificationService; // あなたの既存のNotificationServiceを使用

@Component // @Service でも動作しますが、スケジューラの場合は @Component が一般的です。
public class AttendanceAlert {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceAlert.class); // 追加

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private NotificationService notificationService; // 'service' から 'notificationService' に変更（可読性のため）

    // 毎日21時5分に実行
    // 秒 分 時 日 月 曜日
  //  @Scheduled(cron = "0 5 21 * * *")
    @Scheduled(cron = "0 * * * * *")//テスト用
    public void notifyUnclockedOutUsers() {
        logger.info("未退勤者アラートチェックを開始します。"); // 追加

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now(); // 現在時刻を取得

        // 通知時刻（21:00）を過ぎているかチェック
       // if (now.isAfter(LocalTime.of(21, 0))) { 
            // 今日の出勤記録があり、かつ退勤記録がまだない勤怠データを取得
            // AttendanceRepositoryに新しいメソッドが必要になる可能性が高いです。
            List<Attendance> unclockedAttendances = attendanceRepository.findByAttendanceDateAndClockInNotNullAndClockOutIsNull(today);

            if (!unclockedAttendances.isEmpty()) {
                logger.info("{} 件の未退勤者が見つかりました。通知を送信します。", unclockedAttendances.size()); // 追加
                notificationService.sendUnclockedOutAlert(unclockedAttendances);
            } else {
                logger.info("本日、未退勤者はいませんでした。"); // 追加
            }
       // } else {
       //     logger.info("現在の時刻は通知時刻前です。未退勤者チェックはスキップされました。"); // 追加
      //  }
        logger.info("未退勤者アラートチェックを完了しました。"); // 追加
    }
}