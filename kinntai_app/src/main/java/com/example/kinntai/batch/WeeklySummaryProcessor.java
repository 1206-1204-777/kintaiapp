package com.example.kinntai.batch;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.WeeklySummary;
import com.example.kinntai.repository.AttendanceRepository;

/*週次勤怠情報を集計*/

@Component
public class WeeklySummaryProcessor implements ItemProcessor<User, WeeklySummary> {

	@Autowired
	private AttendanceRepository attendanceRepository;

	@Override
	@Nullable
	public WeeklySummary process(@NonNull User user) throws Exception {
		//バッチの集計対象を計算する
		//取得する日を先週の月曜日から日曜日に設定する
		LocalDate today = LocalDate.now();
		//週の終了を先週の日曜日に設定
		LocalDate weekEnd = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
		//週の開始を先週の月曜日に設定
		LocalDate weekStart = weekEnd.minusDays(6);//weekEndの日付から6日引く
		
//		//	動作確認テスト用コード
//		LocalDate weekEnd = LocalDate.of(2025, 6, 1);
//		LocalDate weekStart = LocalDate.of(2025, 5, 26);
		
		//対象のユーザーの先週の勤怠情報を取得
		List<Attendance> weeklyAttendans = 
				attendanceRepository.findByUserIdAndDateBetween(user.getId(),
				weekStart,weekEnd);
		//勤怠情報がない場合はnullを返す
		if (weeklyAttendans.isEmpty()) {
			return null;
		}

		//各項目を集計する
		int workDays = weeklyAttendans.size();
		long totalWorkMinutes = 0; //勤務時間の初期化
		long totalOvertime = 0; //残業時間の初期化

		//勤務時間のリスト内容を集計
		for (Attendance attendance : weeklyAttendans) {
			//勤怠情報がnullの場合の処理
			totalWorkMinutes += Optional.ofNullable(attendance.getTotalWorkMin()).orElse(0L);
			totalOvertime += Optional.ofNullable(attendance.getOvertimeMinutes()).orElse(0L);

		}

		//Entityに集計した値をセットする
		//集計をした年と月をセット
		WeeklySummary summary = new WeeklySummary();
		summary.setUserId(user.getId());
		summary.setMonth(weekStart.getMonthValue());

		//週番号を取得。週番号とは1年間の週それぞれに割り振られている番号である
		WeekFields fields = WeekFields.of(Locale.JAPAN);
		summary.setWeekNumber(weekStart.get(fields.weekOfWeekBasedYear()));

		//集計した期間をセット
		summary.setStartDate(weekStart);
		summary.setEndDate(weekEnd);
		summary.setWorkDays(workDays);

		//勤務時間を1時間単位に変換
		summary.setTotalWorkHours(totalWorkMinutes / 60.0);
		summary.setOvertimeHours(totalOvertime / 60.0);

		//平均勤務時間を計算してセット
		if (workDays > 0) {
			summary.setAverageWorkHours(totalWorkMinutes / (double) workDays / 60.0);
		} else {
			//もしも勤務していない日があれば0分をセット
			summary.setAverageWorkHours(0.0);
		}
		return summary;
	}

}
