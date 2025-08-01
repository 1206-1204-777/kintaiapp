package com.example.kinntai.service.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.admin.AdminAttendanceUpdateRequest;
import com.example.kinntai.dto.admin.AttendanceStatsResponse;
import com.example.kinntai.dto.admin.TodayAttendanceResponse;
import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.UserRepository;

/**
 * 管理者向け勤怠管理サービス
 * 全社員の勤怠状況確認、統計情報の計算、勤怠データの編集機能を提供
 */
@Service
@Transactional(readOnly = true)
public class AdminAttendanceService {

	@Autowired
	private AttendanceRepository attendanceRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * 今日の勤怠統計データを取得
	 * ダッシュボード表示用の統計情報を計算 (管理者ユーザーを除く)
	 *
	 * @return 勤怠統計データ
	 */
	public AttendanceStatsResponse getTodayAttendanceStats() {
		LocalDate today = LocalDate.now();

		// 全社員を取得し、管理者ユーザーを除外
		List<User> allUsers = userRepository.findAll();
		List<User> filteredUsers = allUsers;

		// 今日の勤怠記録を取得
		List<Attendance> todayAttendances = getAllAttendanceByDate(today);

		// 統計を計算
		int totalEmployees = filteredUsers.size(); // フィルター後の社員数を使用
		int presentCount = 0;
		int working = 0;
		int absentCount = 0;
		int overtimeCount = 0;
		double totalOvertimeHours = 0.0;

		// 勤怠記録をユーザーIDでマップ
		Map<Long, Attendance> attendanceMap = todayAttendances.stream()
				.collect(Collectors.toMap(a -> a.getUser().getId(), a -> a));

		for (User user : filteredUsers) { // フィルターされたユーザーリストに基づいてループ
			Attendance attendance = attendanceMap.get(user.getId());

			if (attendance != null) {
				// ダッシュボード表示のために、取得した勤怠記録の計算値を最新の状態に更新
				// ただし、これはDBに保存しない一時的な計算
				recalculateWorkingHours(attendance);
			}

			if (attendance != null && attendance.getClockIn() != null) {
				presentCount++;

				// 残業時間の計算
				// getOvertimeMinutes()がnullの場合でも0として扱うため、nullチェックを強化
				if (attendance.getOvertimeMinutes() != null && attendance.getOvertimeMinutes() > 0) {
					overtimeCount++;
					totalOvertimeHours += attendance.getOvertimeMinutes() / 60.0;
				}
			} else {
				absentCount++;
			}
		}

		return new AttendanceStatsResponse(
				presentCount,
				working,
				absentCount,
				overtimeCount,
				totalOvertimeHours,
				totalEmployees);
	}

	/**
	 * 今日の全社員勤怠状況一覧を取得
	 * 管理者ユーザーを除外して表示
	 *
	 * @return 今日の勤怠状況一覧 (管理者ユーザーを除く)
	 */
	public List<TodayAttendanceResponse> getTodayAttendanceList() {
		List<User> allUsers = userRepository.findAll();

		// ロールがADMINのユーザーを除外して全社員を取得
		List<User> filteredUsers = allUsers;

		List<Attendance> attendances = getAllAttendanceByDate(LocalDate.now());

		Map<Long, Attendance> attendanceMap = attendances.stream()
				.collect(Collectors.toMap(a -> a.getUser().getId(), a -> a));

		List<TodayAttendanceResponse> result = new ArrayList<>();

		for (User user : filteredUsers) { // フィルターされたユーザーリストを使用
			Attendance attendance = attendanceMap.get(user.getId());

			TodayAttendanceResponse response = new TodayAttendanceResponse();
			response.setId(user.getId());
			response.setName(user.getUsername());
			response.setEmail(user.getEmail());
			response.setDepartment(user.getLocation() != null ? user.getLocation().getName() : "未設定");
			response.setAvatar(user.getUsername().substring(0, 1).toUpperCase());

			// 勤務時間設定（デフォルト値または勤務地の設定値）
			if (user.getLocation() != null) {
				response.setWorkStartTime(user.getLocation().getStartTime().toString());
				response.setWorkEndTime(user.getLocation().getEndTime().toString());
			} else {
				response.setWorkStartTime("09:00");
				response.setWorkEndTime("18:00");
			}

			if (attendance != null) {
				recalculateWorkingHours(attendance);

				response.setAttendanceId(attendance.getId());
				response.setTodayClockIn(
						attendance.getClockIn() != null ? attendance.getClockIn().toLocalTime().toString() : null);
				response.setTodayClockOut(
						attendance.getClockOut() != null ? attendance.getClockOut().toLocalTime().toString() : null);

				// ステータス判定
				// 現在のステータス判定ロジック（約330行目付近）を以下に変更：

				// ステータス判定
				if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
					if (attendance.getOvertimeMinutes() != null && attendance.getOvertimeMinutes() > 0) {
						response.setStatus("overtime");
						response.setOvertimeHours(attendance.getOvertimeMinutes() / 60.0);
						response.setOvertimeType("late");
					} else {
						response.setStatus("present");
						response.setOvertimeHours(0.0);
						response.setOvertimeType(null);
					}
				} else if (attendance.getClockIn() != null) {
					// ★★★ ここを修正：出勤済みで未退勤の場合 ★★★
					if (attendance.getOvertimeMinutes() != null && attendance.getOvertimeMinutes() > 0) {
						response.setStatus("overtime"); // 残業中
					} else {
						response.setStatus("working"); // ★★★ 'working' ステータスを追加 ★★★
					}
					response.setOvertimeHours(
							attendance.getOvertimeMinutes() != null ? attendance.getOvertimeMinutes() / 60.0 : 0.0);
					response.setOvertimeType(null);
				} else {
					response.setStatus("absent");
					response.setTodayClockIn(null);
					response.setTodayClockOut(null);
					response.setOvertimeHours(0.0);
					response.setOvertimeType(null);
				}

				// 個人情報設定
				response.setPhone("090-****-****");
				response.setJoinDate("2023-01-01");
				response.setLocation(response.getDepartment());

				result.add(response);
			}

		}
		return result;
	}

	/**
	 * 指定日の全社員勤怠状況一覧を取得
	 * 管理者ユーザーを除外して表示
	 *
	 * @param date 対象日付
	 * @return 指定日の勤怠状況一覧 (管理者ユーザーを除く)
	 */
	public List<TodayAttendanceResponse> getAttendanceListByDate(LocalDate date) {
		List<User> allUsers = userRepository.findAll();

		// ロールがADMINのユーザーを除外して全社員を取得
		List<User> filteredUsers = allUsers;

		List<Attendance> attendances = getAllAttendanceByDate(date);

		Map<Long, Attendance> attendanceMap = attendances.stream()
				.collect(Collectors.toMap(a -> a.getUser().getId(), a -> a));

		List<TodayAttendanceResponse> result = new ArrayList<>();

		for (User user : filteredUsers) { // フィルターされたユーザーリストを使用
			Attendance attendance = attendanceMap.get(user.getId());

			TodayAttendanceResponse response = new TodayAttendanceResponse();
			response.setId(user.getId());
			response.setName(user.getUsername());
			response.setEmail(user.getEmail());
			response.setDepartment(user.getLocation() != null ? user.getLocation().getName() : "未設定");
			response.setAvatar(user.getUsername().substring(0, 1).toUpperCase());

			// 勤務時間設定（デフォルト値または勤務地の設定値）
			if (user.getLocation() != null) {
				response.setWorkStartTime(user.getLocation().getStartTime().toString());
				response.setWorkEndTime(user.getLocation().getEndTime().toString());
			} else {
				response.setWorkStartTime("09:00");
				response.setWorkEndTime("18:00");
			}

			if (attendance != null) {
				recalculateWorkingHours(attendance);

				response.setAttendanceId(attendance.getId());
				response.setTodayClockIn(
						attendance.getClockIn() != null ? attendance.getClockIn().toLocalTime().toString() : null);
				response.setTodayClockOut(
						attendance.getClockOut() != null ? attendance.getClockOut().toLocalTime().toString() : null);

				// ステータス判定
				// 現在のステータス判定ロジック（約330行目付近）を以下に変更：

				// ステータス判定
				if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
					if (attendance.getOvertimeMinutes() != null && attendance.getOvertimeMinutes() > 0) {
						response.setStatus("overtime");
						response.setOvertimeHours(attendance.getOvertimeMinutes() / 60.0);
						response.setOvertimeType("late");
					} else {
						response.setStatus("present");
						response.setOvertimeHours(0.0);
						response.setOvertimeType(null);
					}
				} else if (attendance.getClockIn() != null) {
					// ★★★ ここを修正：出勤済みで未退勤の場合 ★★★
					if (attendance.getOvertimeMinutes() != null && attendance.getOvertimeMinutes() > 0) {
						response.setStatus("overtime"); // 残業中
					} else {
						response.setStatus("working"); // ★★★ 'working' ステータスを追加 ★★★
					}
					response.setOvertimeHours(
							attendance.getOvertimeMinutes() != null ? attendance.getOvertimeMinutes() / 60.0 : 0.0);
					response.setOvertimeType(null);
				} else {
					response.setStatus("absent");
					response.setTodayClockIn(null);
					response.setTodayClockOut(null);
					response.setOvertimeHours(0.0);
					response.setOvertimeType(null);
				}

				// 個人情報設定
				response.setPhone("090-****-****");
				response.setJoinDate("2023-01-01");
				response.setLocation(response.getDepartment());

				result.add(response);
			}

		}
		return result;
		}

	/**
	 * 管理者による勤怠データの更新
	 * 社員の出勤・退勤時刻を管理者権限で修正
	 *
	 * @param attendanceId 勤怠記録ID
	 * @param request 修正内容
	 * @return 修正結果
	 */
	@Transactional
	public void updateAttendanceByAdmin(Long attendanceId, AdminAttendanceUpdateRequest request) {
		Attendance attendance = attendanceRepository.findById(attendanceId)
				.orElseThrow(() -> new IllegalArgumentException("指定された勤怠記録が見つかりません"));

		// 出勤時刻の更新
		if (request.getClockInTime() != null) {
			LocalDateTime clockInDateTime = LocalDateTime.of(attendance.getDate(), request.getClockInTime());
			attendance.setClockIn(clockInDateTime);
		} else {
			attendance.setClockIn(null);
		}

		// 退勤時刻の更新
		if (request.getClockOutTime() != null) {
			LocalDateTime clockOutDateTime = LocalDateTime.of(attendance.getDate(), request.getClockOutTime());
			// 退勤時刻が出勤時刻より前の場合は翌日として扱う
			if (attendance.getClockIn() != null && request.getClockOutTime().isBefore(request.getClockInTime())) {
				clockOutDateTime = clockOutDateTime.plusDays(1);
			}
			attendance.setClockOut(clockOutDateTime);
		} else {
			attendance.setClockOut(null);
		}

		// 勤務時間と残業時間の再計算
		recalculateWorkingHours(attendance);

		attendance.setUpdatedAt(LocalDateTime.now());
		attendanceRepository.save(attendance);
	}

	/**
	 * 期間指定での勤怠データ取得
	 * 管理者ユーザーを除外して表示
	 *
	 * @param startDate 開始日
	 * @param endDate 終了日
	 * @param userId ユーザーID (オプション、指定時は特定ユーザーのみ)
	 * @return 期間内の勤怠データ (管理者ユーザーを除く)
	 */
	public List<TodayAttendanceResponse> getAttendanceByPeriod(LocalDate startDate, LocalDate endDate, Long userId) {
		List<Attendance> attendances;
		if (userId != null) {
			// 特定ユーザーの勤怠データを取得し、そのユーザーが管理者でないことを確認
			attendances = attendanceRepository.findByUser_IdAndDateBetweenOrderByDateAsc(userId, startDate, endDate)
					.stream()
					.filter(attendance -> !attendance.getUser().getRole().name().equals("ADMIN")) // ★修正: enumのname()メソッドで比較
					.collect(Collectors.toList());
		} else {
			// 全ユーザーの勤怠データを取得し、管理者ユーザーを除外
			attendances = attendanceRepository.findByDateBetween(startDate, endDate).stream()
					.filter(attendance -> !attendance.getUser().getRole().name().equals("ADMIN")) // ★修正: enumのname()メソッドで比較
					.collect(Collectors.toList());
		}
		attendances.forEach(this::recalculateWorkingHours);
		return convertToTodayAttendanceResponse(attendances);
	}

	/**
	 * 期間とユーザー指定での勤怠データ取得
	 *
	 * @param startDate 開始日
	 * @param endDate 終了日
	 * @param userId ユーザーID
	 * @return 期間内の特定ユーザーの勤怠データ
	 */
	public List<TodayAttendanceResponse> getAttendanceByPeriodAndUser(LocalDate startDate, LocalDate endDate,
			Long userId) {
		List<Attendance> attendances = attendanceRepository
				.findByUser_IdAndDateBetweenOrderByDateAsc(userId, startDate, endDate).stream()
				.filter(attendance -> !attendance.getUser().getRole().name().equals("ADMIN")) // ★修正: enumのname()メソッドで比較
				.collect(Collectors.toList());
		attendances.forEach(this::recalculateWorkingHours);
		return convertToTodayAttendanceResponse(attendances);
	}

	/**
	 * 未退勤者の一覧を取得
	 * 管理者ユーザーを除外して表示
	 *
	 * @param date 対象日付
	 * @return 未退勤者一覧 (管理者ユーザーを除く)
	 */
	public List<TodayAttendanceResponse> getUnclockedOutUsers(LocalDate date) {
		List<Attendance> unclockedAttendances = attendanceRepository.findByDateAndClockInNotNullAndClockOutIsNull(date)
				.stream()
				.filter(attendance -> !attendance.getUser().getRole().name().equals("ADMIN")) // ★修正: enumのname()メソッドで比較
				.collect(Collectors.toList());
		unclockedAttendances.forEach(this::recalculateWorkingHours);
		return convertToTodayAttendanceResponse(unclockedAttendances);
	}

	/**
	 * 指定日のすべての勤怠記録を取得（内部用ヘルパーメソッド）
	 */
	private List<Attendance> getAllAttendanceByDate(LocalDate date) {
		// 特定の日付の全勤怠記録を検索するための新しいクエリを使用
		// ここではロールによるフィルターは行わない (getTodayAttendanceList()などでフィルターするため)
		return attendanceRepository.findByDateExact(date);
	}

	/**
	 * Attendanceエンティティのリストをレスポンス形式に変換
	 */
	private List<TodayAttendanceResponse> convertToTodayAttendanceResponse(List<Attendance> attendances) {
		return attendances.stream().map(attendance -> {
			TodayAttendanceResponse response = new TodayAttendanceResponse();
			User user = attendance.getUser();

			response.setId(user.getId());
			response.setAttendanceId(attendance.getId());
			response.setName(user.getUsername());
			response.setEmail(user.getEmail());
			response.setDepartment(user.getLocation() != null ? user.getLocation().getName() : "未設定");
			response.setAvatar(user.getUsername().substring(0, 1).toUpperCase());

			// 勤務時間設定
			if (user.getLocation() != null) {
				response.setWorkStartTime(user.getLocation().getStartTime().toString());
				response.setWorkEndTime(user.getLocation().getEndTime().toString());
			} else {
				response.setWorkStartTime("09:00");
				response.setWorkEndTime("18:00");
			}

			response.setTodayClockIn(
					attendance.getClockIn() != null ? attendance.getClockIn().toLocalTime().toString() : null);
			response.setTodayClockOut(
					attendance.getClockOut() != null ? attendance.getClockOut().toLocalTime().toString() : null);

			// ステータス判定
			// 現在のステータス判定ロジック（約330行目付近）を以下に変更：

			// ステータス判定
			if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
				if (attendance.getOvertimeMinutes() != null && attendance.getOvertimeMinutes() > 0) {
					response.setStatus("overtime");
					response.setOvertimeHours(attendance.getOvertimeMinutes() / 60.0);
					response.setOvertimeType("late");
				} else {
					response.setStatus("present");
					response.setOvertimeHours(0.0);
					response.setOvertimeType(null);
				}
			} else if (attendance.getClockIn() != null) {
				// ★★★ ここを修正：出勤済みで未退勤の場合 ★★★
				if (attendance.getOvertimeMinutes() != null && attendance.getOvertimeMinutes() > 0) {
					response.setStatus("overtime"); // 残業中
				} else {
					response.setStatus("working"); // ★★★ 'working' ステータスを追加 ★★★
				}
				response.setOvertimeHours(
						attendance.getOvertimeMinutes() != null ? attendance.getOvertimeMinutes() / 60.0 : 0.0);
				response.setOvertimeType(null);
			} else {
				response.setStatus("absent");
				response.setTodayClockIn(null);
				response.setTodayClockOut(null);
				response.setOvertimeHours(0.0);
				response.setOvertimeType(null);
			}

			// 個人情報設定
			response.setPhone("090-****-****");
			response.setJoinDate("2023-01-01");
			response.setLocation(response.getDepartment());

			return response;
		}).collect(Collectors.toList());
	}

	/**
	 * 勤務時間と残業時間を再計算
	 * 勤務中の場合は現在時刻までで計算し、nullではなく0Lを返す
	 */
	private void recalculateWorkingHours(Attendance attendance) {
		if (attendance.getClockIn() != null) {
			LocalDateTime effectiveClockOut = attendance.getClockOut();
			boolean isCurrentlyClockedIn = (effectiveClockOut == null);
			if (isCurrentlyClockedIn) {
				effectiveClockOut = LocalDateTime.now();
			}

			long totalMinutes = 0;
			if (effectiveClockOut.isAfter(attendance.getClockIn())) {
				totalMinutes = java.time.Duration.between(attendance.getClockIn(), effectiveClockOut).toMinutes();
			}

			long breakMinutes = 0L;
			if (totalMinutes > 60) {
				breakMinutes = 60L;
			}
			attendance.setTotalBreakMin(breakMinutes);

			long actualWorkMinutes = Math.max(0, totalMinutes - breakMinutes);
			attendance.setTotalWorkMin(actualWorkMinutes);

			long overtimeMinutes = 0L;
			User user = attendance.getUser();
			if (user.getLocation() != null) {
				LocalTime regularStartTime = user.getLocation().getStartTime();
				LocalTime regularEndTime = user.getLocation().getEndTime();

				long regularWorkMinutes = 0;
				if (regularEndTime.isAfter(regularStartTime)) {
					long regularTotalDuration = java.time.Duration.between(regularStartTime, regularEndTime)
							.toMinutes();
					regularWorkMinutes = Math.max(0, regularTotalDuration - breakMinutes);
				} else {
					regularWorkMinutes = 8 * 60 - breakMinutes;
				}

				overtimeMinutes = Math.max(0, actualWorkMinutes - regularWorkMinutes);

			} else {
				long defaultWorkMinutes = 8 * 60;
				long defaultRegularWorkMinutes = Math.max(0, defaultWorkMinutes - breakMinutes);
				overtimeMinutes = Math.max(0, actualWorkMinutes - defaultRegularWorkMinutes);
			}
			attendance.setOvertimeMinutes(overtimeMinutes);

		} else {
			attendance.setTotalBreakMin(0L);
			attendance.setTotalWorkMin(0L);
			attendance.setOvertimeMinutes(0L);
		}
	}
}
