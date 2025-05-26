package com.example.kinntai.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.PersonalHolidayRequest; // DTOは後で作成します
import com.example.kinntai.entity.HolidayType; // Enum
import com.example.kinntai.entity.PersonalHoliday;
import com.example.kinntai.entity.RequestStatus; // Enum
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.PersonalHolidayRepository;
import com.example.kinntai.repository.UserRepository; // Userエンティティを使うため
import com.example.kinntai.service.PersonalHolidayService;

@Service
public class PersonalHolidayServiceImpl implements PersonalHolidayService {

    @Autowired
    private PersonalHolidayRepository personalHolidayRepository;

    @Autowired
    private UserRepository userRepository; // Userエンティティを取得するため

    @Override
    @Transactional
    public PersonalHoliday applyPersonalHoliday(PersonalHolidayRequest request) {
        // ユーザーの存在チェック
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("指定されたユーザーが見つかりません。"));

        // 日付の重複チェックや過去日の申請禁止などのバリデーションを追加することもできます
        // Optional<PersonalHoliday> existingHoliday = personalHolidayRepository.findByUser_IdAndHolidayDate(user.getId(), request.getHolidayDate());
        // if (existingHoliday.isPresent()) {
        //     throw new IllegalArgumentException("指定された日付は既に休日として申請されています。");
        // }

        PersonalHoliday personalHoliday = new PersonalHoliday();
        personalHoliday.setUser(user);
        personalHoliday.setHolidayDate(request.getHolidayDate());
        personalHoliday.setHolidayType(HolidayType.valueOf(request.getHolidayType().toUpperCase())); // StringからEnumに変換
        personalHoliday.setReason(request.getReason());
        personalHoliday.setStatus(RequestStatus.PENDING); // 申請時は常にPENDING
        personalHoliday.setCreatedAt(LocalDateTime.now());
        personalHoliday.setUpdatedAt(LocalDateTime.now());

        return personalHolidayRepository.save(personalHoliday);
    }

    @Override
    public List<PersonalHoliday> getPersonalHolidaysByUserId(Long userId) {
        return personalHolidayRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Optional<PersonalHoliday> getPersonalHolidayById(Long id) {
        return personalHolidayRepository.findById(id);
    }

    @Override
    @Transactional
    public PersonalHoliday updatePersonalHolidayStatus(Long id, RequestStatus status, Long approverUserId) {
        PersonalHoliday personalHoliday = personalHolidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定された個人休日申請が見つかりません。"));

        User approver = userRepository.findById(approverUserId)
                .orElseThrow(() -> new IllegalArgumentException("指定された承認者ユーザーが見つかりません。"));

        // 管理者権限のチェックなどを追加することもできます
        // if (approver.getRole() != UserRole.ADMIN) {
        //     throw new AccessDeniedException("この操作は管理者のみが実行できます。");
        // }

        personalHoliday.setStatus(status);
        personalHoliday.setApprover(approver);
        personalHoliday.setUpdatedAt(LocalDateTime.now());

        return personalHolidayRepository.save(personalHoliday);
    }

    @Override
    @Transactional
    public boolean cancelPersonalHoliday(Long id, Long userId) {
        PersonalHoliday personalHoliday = personalHolidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定された個人休日申請が見つかりません。"));

        // 申請者本人か、または管理者のみがキャンセルできるようにする
        if (!personalHoliday.getUser().getId().equals(userId) /*&& userRole != UserRole.ADMIN*/) {
            throw new SecurityException("この申請は、申請者本人または管理者のみがキャンセルできます。");
        }

        // 申請中 (PENDING) のみキャンセル可能にする
        if (personalHoliday.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("申請中の休日のみキャンセル可能です。");
        }
        
        // ステータスをREJECTEDに更新するか、削除するか、要件によって調整
        // 今回はREJECTEDに更新する例
        personalHoliday.setStatus(RequestStatus.REJECTED);
        personalHoliday.setUpdatedAt(LocalDateTime.now());
        personalHolidayRepository.save(personalHoliday); // 削除する場合は deleteById(id)

        return true;
    }
}