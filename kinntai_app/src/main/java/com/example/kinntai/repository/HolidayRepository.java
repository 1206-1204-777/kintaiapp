// HolidayRepository.java
package com.example.kinntai.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.Holiday;
import com.example.kinntai.entity.User;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

	/* ユーザーごとの休日が登録されているか名前で検索*/
	boolean existsByUserAndDate(User user, LocalDate date);
	
	/*特定のユーザーの休日を名前を使って取得*/
	List<Holiday> findByUserAndDateBetweenOrderByDateAsc(User user,LocalDate startDate, LocalDate endDate);

	// 期間内の休日を取得
	//    @Query("SELECT h FROM Holiday h WHERE h.date BETWEEN :startDate AND :endDate ORDER BY h.date")
	//    List<Holiday> findHolidaysBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
	//    
	//    // 特定年月の休日数を取得
	//    @Query("SELECT COUNT(h) FROM Holiday h WHERE YEAR(h.date) = :year AND MONTH(h.date) = :month")
	//    int countHolidaysInMonth(@Param("year") int year, @Param("month") int month);
}