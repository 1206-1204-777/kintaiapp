package com.example.kinntai.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.Break;

public interface BreakRepository extends JpaRepository<Break,Long>{
	/*休憩記録の取得*/
	List<Break> findByAttendance(Attendance attndance);

	/*終了時間のない休憩を取得*/
	Optional<Break> findByAttendanceAndBreakEndIsNull(Attendance attendance);

	Optional<Break> findByAttendanceAndBreakEndIsNotNull(Attendance attendance);
}
