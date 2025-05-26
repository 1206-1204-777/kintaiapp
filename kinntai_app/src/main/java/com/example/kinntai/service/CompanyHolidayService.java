package com.example.kinntai.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.kinntai.dto.CompanyHolidayRequest; // DTOは後で作成します
import com.example.kinntai.entity.CompanyHoliday;

public interface CompanyHolidayService {

    // 会社休日の登録
    CompanyHoliday createCompanyHoliday(CompanyHolidayRequest request);

    // すべての会社休日を取得 (日付順)
    List<CompanyHoliday> getAllCompanyHolidays();

    // 特定の会社休日をIDで取得
    Optional<CompanyHoliday> getCompanyHolidayById(Long id);

    // 特定の日付の会社休日を取得
    Optional<CompanyHoliday> getCompanyHolidayByDate(LocalDate date);

    // 会社休日の更新
    CompanyHoliday updateCompanyHoliday(Long id, CompanyHolidayRequest request);

    // 会社休日の削除
    boolean deleteCompanyHoliday(Long id);
}