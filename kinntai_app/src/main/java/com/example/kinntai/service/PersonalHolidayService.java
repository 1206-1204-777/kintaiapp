package com.example.kinntai.service;

import java.util.List;
import java.util.Optional;

import com.example.kinntai.dto.PersonalHolidayRequest; // DTOは後で作成します
import com.example.kinntai.entity.PersonalHoliday;
import com.example.kinntai.entity.RequestStatus;

public interface PersonalHolidayService {

    // 個人休日の申請
    PersonalHoliday applyPersonalHoliday(PersonalHolidayRequest request);

    // 特定のユーザーのすべての個人休日申請を取得
    List<PersonalHoliday> getPersonalHolidaysByUserId(Long userId);

    // 特定の個人休日申請をIDで取得
    Optional<PersonalHoliday> getPersonalHolidayById(Long id);

    // 個人休日申請のステータスを更新 (承認/却下)
    PersonalHoliday updatePersonalHolidayStatus(Long id, RequestStatus status, Long approverUserId);

    // 個人休日申請をキャンセル (ユーザー自身が申請中のものをキャンセル)
    boolean cancelPersonalHoliday(Long id, Long userId);
}