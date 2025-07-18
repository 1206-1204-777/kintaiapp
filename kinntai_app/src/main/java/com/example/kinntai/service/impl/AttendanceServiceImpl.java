package com.example.kinntai.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.AttendanceResponse;
import com.example.kinntai.dto.UserAttendanceUpdateRequestDto;
import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.Location;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.WorkType;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.AttendanceService;

@Service
public class AttendanceServiceImpl implements AttendanceService {

	@Autowired
	private AttendanceRepository attendanceRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * 出勤処理
	 */
	@Override
	@Transactional
	public Attendance clockIn(Long userId, String type) {
		try {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

			LocalDate today = LocalDate.now();

			// 当日の勤怠記録を取得または作成
			Attendance attendance = attendanceRepository.findByUser_IdAndDate(userId, today)
					.orElse(new Attendance(user, today));
			// 既に出勤済みの場合はエラー
			if (attendance.getClockIn() != null) {
				throw new RuntimeException("すでに出勤済みです");
			}

			// 出勤時刻を設定
			attendance.setClockIn(LocalDateTime.now());

			if (attendance.getTotalBreakMin() == null) {
				attendance.setTotalBreakMin(0L);
			}
			if (attendance.getTotalWorkMin() == null) {
				attendance.setTotalWorkMin(0L);
			}
			
			attendance.setWorkType(WorkType.valueOf(type));

			return attendanceRepository.save(attendance);
		} catch (Exception e) {
			System.err.println("出勤処理エラー: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	

	/**
	 * 退勤処理
	 */
	@Override
	@Transactional
	public Attendance clockOut(Long userId) {
		try {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

            // 現在時刻（日付情報も含む）
            LocalDateTime now = LocalDateTime.now(); 

            // 🚨 修正点: 最も新しい「出勤済みで未退勤」の記録を検索
            // AttendanceRepositoryに findByUser_IdAndClockInIsNotNullAndClockOutIsNullOrderByDateDesc(Long userId) が必要
            Optional<Attendance> attendanceOpt = attendanceRepository.findByUser_IdAndClockInIsNotNullAndClockOutIsNullOrderByDateDesc(userId);

            // アクティブな勤怠記録が見つからなければエラー
			Attendance attendance = attendanceOpt
					.orElseThrow(() -> new RuntimeException("本日の出勤記録がありません。"));
			
			// 既に退勤済みの場合はエラー（findByUser_IdAndClockInIsNotNullAndClockOutIsNullOrderByDateDesc で取得しているため、基本的には不要だが念のため）
			if (attendance.getClockOut() != null) {
				throw new RuntimeException("すでに退勤済みです");
			}

			// 退勤時刻を設定
			attendance.setClockOut(now); // 現在時刻を設定
			attendance.setUpdatedAt(LocalDateTime.now());

			// 1. 総勤務時間（出勤から退勤までの時間）を計算
			// Duration.between() は LocalDateTime 型の比較に使用します。
			long totalDurationMinutes = Duration.between(
					attendance.getClockIn(), attendance.getClockOut()).toMinutes();

			// 2. 一律1時間（60分）の休憩時間を設定
			long fixedBreakMinutes = 60L;
			attendance.setTotalBreakMin(fixedBreakMinutes); // totalBreakMinutes に一律60分を設定

			// 3. 実労働時間（総勤務時間から休憩時間を差し引いたもの）を計算し、totalWorkMinutes に設定
			// 休憩時間を引いた結果が負にならないように Math.max を使用
			long actualWorkMinutes = Math.max(0, totalDurationMinutes - fixedBreakMinutes);
			attendance.setTotalWorkMin(actualWorkMinutes); // totalWorkMinutes に実労働時間を設定

			/*残業時間の計算*/
			Long overtime = calculateOvertime(
					user, attendance.getClockIn(), attendance.getClockOut(), actualWorkMinutes);
			attendance.setOvertimeMinutes(overtime);

			return attendanceRepository.save(attendance);
		} catch (Exception e) {
			System.err.println("退勤処理エラー: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 現在の勤務状況を取得
	 */
	@Override
	public AttendanceResponse getAttendanceStatus(Long userId) {
		try {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

            // 🚨 修正点: 最も新しい「出勤済みで未退勤」の記録を検索
            Optional<Attendance> attendanceOpt = attendanceRepository.findByUser_IdAndClockInIsNotNullAndClockOutIsNullOrderByDateDesc(userId);

			AttendanceResponse response = new AttendanceResponse();
			response.setUserId(userId);
			response.setDate(LocalDate.now()); // レスポンスの日付はあくまで今日の日付

			if (attendanceOpt.isPresent()) {
				Attendance attendance = attendanceOpt.get();
				response.setId(attendance.getId());
				response.setClockIn(attendance.getClockIn());
				response.setClockOut(attendance.getClockOut());

				// 勤務中かどうかの判定
				// clockInが存在し、かつclockOutがnullの場合のみworkingをtrueにする
				response.setWorking(attendance.getClockIn() != null && attendance.getClockOut() == null);
			} else {
				// 勤怠記録自体が存在しない場合はworkingをfalseにする
				response.setWorking(false);
			}

			return response;
		} catch (Exception e) {
			System.err.println("勤務状態確認エラー: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	//退勤していないユーザーのリスト作成
	@Override
	public List<AttendanceResponse> getUnclockedUsersToday() throws RuntimeException {
		LocalDate today = LocalDate.now();
		List<Attendance> attendanses = attendanceRepository.findClickedOutToday(today);

		return attendanses.stream()
				.map(AttendanceResponse::fromEntity)

				.collect(Collectors.toList());
	}

	/**
	 * 特定の日の勤怠情報を取得
	 */
	@Override
	public Optional<Attendance> getAttendanceByDate(Long userId, LocalDate date) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

		return attendanceRepository.findByUser_IdAndDate(userId, date);
	}

	/**
	 * 月次の勤怠情報を取得
	 */
	@Override
	public List<Attendance> getMonthlyAttendance(Long userId, int year, int month) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate startDate = yearMonth.atDay(1);
		LocalDate endDate = yearMonth.atEndOfMonth();

		return attendanceRepository.findByUser_IdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);
	}

	/**
	 * 月次の勤怠情報を取得（文字列指定）
	 */
	@Override
	public List<Attendance> getMonthlyAttendance(Long userId, String yearMonth) {
		try {
			System.out.println("勤怠履歴取得: userId.getId().getId=" + userId + ", month=" + yearMonth);

			// yearMonth 形式は "YYYY-MM"
			String[] parts = yearMonth.split("-");
			if (parts.length != 2) {
				throw new IllegalArgumentException("月の形式が不正です。YYYY-MM 形式で指定してください。");
			}

			int year = Integer.parseInt(parts[0]);
			int month = Integer.parseInt(parts[1]);

			return getMonthlyAttendance(userId, year, month);
		} catch (Exception e) {
			System.err.println("月次勤怠取得エラー: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 期間内の勤怠情報を月曜～日曜の週で生成（存在しない日も含める）
	 */
	@Override
	public List<Attendance> generateWeeklyAttendances(Long userId, LocalDate startDate, LocalDate endDate) {
		List<Attendance> attendances = attendanceRepository.findByUser_IdAndDateBetweenOrderByDateAsc(
				userId, startDate, endDate);

		List<Attendance> result = new ArrayList<>();

		// 存在する勤怠情報をマップに格納
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			final LocalDate currentDate = date;

			// 既存の勤怠情報があればそれを使う、なければ新しく作る
			Attendance attendance = attendances.stream()
					.filter(a -> a.getDate().equals(currentDate))
					.findFirst()
					.orElse(null);

			if (attendance == null) {
				User user = new User();
				user.setId(userId);

				attendance = new Attendance();
				attendance.setUser(user);
				attendance.setDate(currentDate);
			}

			result.add(attendance);
		}

		return result;
	}

	/*ユーザー情報の取得*/
	@Override
	public List<AttendanceResponse> getAllUser() {

		return attendanceRepository.findAll()
				.stream()
				.map(AttendanceResponse::fromEntity)

				.collect(Collectors.toList());

	}

	@Override
	public List<AttendanceResponse> getAttendanceUser(Long userId) {

		return attendanceRepository.findByUserId(userId)
				.stream()
				.map(AttendanceResponse::fromEntity)
				.collect(Collectors.toList());

	}

		/*勤怠時刻の修正
		 * Attendances登録された定時データを修正*/
		@Override
		public Attendance updateUserAttendance(Long userId, UserAttendanceUpdateRequestDto request)
				throws IllegalAccessException {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException("指定されたユーザーが存在しません。" + userId));
	
			/*時刻とユーザーidで修正対象を検索*/
			List<Attendance> existingAttendanceOptional = attendanceRepository.findAllByUser_IdAndDate(userId,
					request.getDate());
			/*修正対象があれば修正をする*/
			Attendance attendance = existingAttendanceOptional.stream()
					.max(Comparator.comparing(Attendance::getCreatedAt))
					.orElseThrow(() -> new IllegalAccessException("指定された日付の勤怠記録が見つかりません。"));
	
			/*勤務開始の修正*/
			if (request.getStartTime() != null) {
				attendance.setClockIn(LocalDateTime.of(request.getDate(), request.getStartTime()));
			} else {
				attendance.setClockIn(null);//値が入力されてない場合はnullを格納
			}
	
			/*退勤時間の修正*/
			if (request.getEndTime() != null) {
				LocalDateTime clockOutDateTime = LocalDateTime.of(request.getDate(), request.getEndTime());
				/*退勤時刻が出勤時刻より前（０時過ぎ）の場合は翌日の勤怠として扱う*/
				if (attendance.getClockIn() != null && request.getEndTime().isBefore(request.getStartTime())) {
					clockOutDateTime = clockOutDateTime.plusDays(1);
				}
	
				attendance.setClockOut(clockOutDateTime);
			} else {
				attendance.setClockOut(null);
			}
	
			/*休憩時間修正後にも60分の休憩時間を適用*/
			if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
				/*勤務時刻を取得し分単位で比較*/
				long totalDurationMinutes = Duration.between(
						attendance.getClockIn(), attendance.getClockOut()).toMinutes();
				/*休憩時間を格納*/
				long breakMinutes = 60L;
				attendance.setTotalBreakMin(breakMinutes);
	
				/*休憩時間を計算*/
				long actualWorkMinutes = Math.max(0, totalDurationMinutes - breakMinutes);
				attendance.setTotalWorkMin(actualWorkMinutes);
	
				Long overtime = calculateOvertime(
						user, attendance.getClockIn(),attendance.getClockOut(),actualWorkMinutes);
				attendance.setOvertimeMinutes(overtime);
			} else {
				attendance.setTotalBreakMin(null);
				attendance.setTotalWorkMin(null);
			}
	
			attendance.setUpdatedAt(LocalDateTime.now());
			return attendanceRepository.save(attendance);
		}

	/*残業時間の計算のヘルパー（補助）メソッド
	 * 残業計算部分を切り離して活用*/
	private Long calculateOvertime(
			User user, LocalDateTime clockIn, LocalDateTime clockOut, long actualWorkMinutes) {

		System.out.println("--- calculateOvertime Start ---");
		System.out.println("User ID: " + user.getId());
		System.out.println("Clock In: " + clockIn);
		System.out.println("Clock Out: " + clockOut);
		System.out.println("Actual Work Minutes (休憩差し引き後): " + actualWorkMinutes);

		/*ユーザーの勤務表情報を取得*/
		Location location = user.getLocation();
		if (location == null) {
			System.err.println("Error: Location is null for user ID: " + user.getId() + ". Overtime will be 0.");
			return 0L; // 勤務地が設定されていない場合は残業時間0とする
		}

		System.out.println("Location Name: " + location.getName());
		LocalTime regularStartTime = location.getStartTime();
		LocalTime regularEndTime = location.getEndTime();
		System.out.println("Regular Start Time (Location): " + regularStartTime);
		System.out.println("Regular End Time (Location): " + regularEndTime);

		/*取得した定時時間をLocalDateTimeに変換*/
		LocalDateTime regularStartDateTime = LocalDateTime.of(clockIn.toLocalDate(), regularStartTime);
		LocalDateTime regularEndDateTime = LocalDateTime.of(clockOut.toLocalDate(), regularEndTime);
		System.out.println("Regular Start DateTime: " + regularStartDateTime);
		System.out.println("Regular End DateTime (initial): " + regularEndDateTime);

		/*もし定時終了時刻が定時開始時刻より前の場合は翌日の日付とみなす（例: 22:00-翌日07:00）*/
		if (regularEndDateTime.isBefore(regularStartDateTime)) {
			regularEndDateTime = regularEndDateTime.plusDays(1);
			System.out.println("Regular End DateTime (adjusted for next day): " + regularEndDateTime);
		}

		/*定時時間*/
		long regularWorkDurationMinutes = Duration.between(regularStartDateTime, regularEndDateTime).toMinutes();
		System.out.println("Regular Work Duration Minutes (定時総時間): " + regularWorkDurationMinutes);

		/*定時から休憩時間を引く*/
		long regularWorkMinutesAfterBreak = Math.max(0, regularWorkDurationMinutes - 60L); // 定時時間から一律60分を引く
		System.out.println("Regular Work Minutes After Break (定時実働時間): " + regularWorkMinutesAfterBreak);

		long overtime = Math.max(0, actualWorkMinutes - regularWorkMinutesAfterBreak);
		System.out.println("Calculated Overtime Minutes: " + overtime);
		System.out.println("--- calculateOvertime End ---");

		return overtime;

	}
}
