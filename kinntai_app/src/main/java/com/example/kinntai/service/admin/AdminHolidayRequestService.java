package com.example.kinntai.service.admin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.admin.AdminHolidayRequestResponse;
import com.example.kinntai.dto.admin.HolidayRequestActionRequest;
import com.example.kinntai.entity.PersonalHoliday;
import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.PersonalHolidayRepository;
import com.example.kinntai.repository.UserRepository;

/**
 * 管理者向け休暇申請管理サービス
 * 全社員の休暇申請の確認、承認・却下処理を提供
 */
@Service
@Transactional(readOnly = true)
public class AdminHolidayRequestService {

    @Autowired
    private PersonalHolidayRepository personalHolidayRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 全ての休暇申請を取得（フィルタリング可能）
     *
     * @param status 申請ステータス（省略可能）
     * @param userId ユーザーID（省略可能）
     * @return 休暇申請一覧
     */
    public List<AdminHolidayRequestResponse> getAllHolidayRequests(String status, Long userId) {
        List<PersonalHoliday> holidays;
        
        if (userId != null) {
            // 特定ユーザーの申請を取得
            if (status != null && !status.isEmpty()) {
                holidays = personalHolidayRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(
                    userId, status.toUpperCase());
            } else {
                holidays = personalHolidayRepository.findByUser_IdOrderByCreatedAtDesc(userId);
            }
        } else {
            // 全ユーザーの申請を取得
            holidays = personalHolidayRepository.findAll();
            
            // ステータスでフィルタリング
            if (status != null && !status.isEmpty()) {
                RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
                holidays = holidays.stream()
                        .filter(h -> h.getStatus() == requestStatus)
                        .collect(Collectors.toList());
            }
            
            // 作成日時の降順でソート
            holidays = holidays.stream()
                    .sorted((h1, h2) -> h2.getCreatedAt().compareTo(h1.getCreatedAt()))
                    .collect(Collectors.toList());
        }
        
        return convertToAdminHolidayRequestResponse(holidays);
    }

    /**
     * 承認待ちの休暇申請のみを取得
     *
     * @return 承認待ちの休暇申請一覧
     */
    public List<AdminHolidayRequestResponse> getPendingHolidayRequests() {
        List<PersonalHoliday> pendingHolidays = personalHolidayRepository.findAll().stream()
                .filter(h -> h.getStatus() == RequestStatus.PENDING)
                .sorted((h1, h2) -> h1.getCreatedAt().compareTo(h2.getCreatedAt())) // 古い順
                .collect(Collectors.toList());
        
        return convertToAdminHolidayRequestResponse(pendingHolidays);
    }

    /**
     * 特定の休暇申請の詳細を取得
     *
     * @param requestId 申請ID
     * @return 休暇申請の詳細情報
     */
    public AdminHolidayRequestResponse getHolidayRequestById(Long requestId) {
        PersonalHoliday holiday = personalHolidayRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("指定された休暇申請が見つかりません"));
        
        return convertToAdminHolidayRequestResponse(holiday);
    }

    /**
     * 休暇申請を承認
     *
     * @param requestId 申請ID
     * @param actionRequest 承認処理の詳細
     */
    @Transactional
    public void approveHolidayRequest(Long requestId, HolidayRequestActionRequest actionRequest) {
        PersonalHoliday holiday = personalHolidayRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("指定された休暇申請が見つかりません"));

        // 既に処理済みの場合はエラー
        if (holiday.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("この申請は既に処理済みです");
        }

        // 承認者の存在確認
        User approver = userRepository.findById(actionRequest.getApproverId())
                .orElseThrow(() -> new IllegalArgumentException("指定された承認者が見つかりません"));

        // 申請を承認
        holiday.setStatus(RequestStatus.APPROVED);
        holiday.setApprover(approver);
        holiday.setUpdatedAt(LocalDateTime.now());

        personalHolidayRepository.save(holiday);
    }

    /**
     * 休暇申請を却下
     *
     * @param requestId 申請ID
     * @param actionRequest 却下処理の詳細
     */
    @Transactional
    public void rejectHolidayRequest(Long requestId, HolidayRequestActionRequest actionRequest) {
        PersonalHoliday holiday = personalHolidayRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("指定された休暇申請が見つかりません"));

        // 既に処理済みの場合はエラー
        if (holiday.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("この申請は既に処理済みです");
        }

        // 却下者の存在確認
        User approver = userRepository.findById(actionRequest.getApproverId())
                .orElseThrow(() -> new IllegalArgumentException("指定された却下者が見つかりません"));

        // 申請を却下
        holiday.setStatus(RequestStatus.REJECTED);
        holiday.setApprover(approver);
        holiday.setUpdatedAt(LocalDateTime.now());

        personalHolidayRepository.save(holiday);
    }

    /**
     * 休暇申請の統計情報を取得
     *
     * @return 休暇申請の統計情報
     */
    public Map<String, Object> getHolidayRequestStats() {
        List<PersonalHoliday> allRequests = personalHolidayRepository.findAll();
        
        long pendingCount = allRequests.stream()
                .filter(h -> h.getStatus() == RequestStatus.PENDING)
                .count();
        
        long approvedCount = allRequests.stream()
                .filter(h -> h.getStatus() == RequestStatus.APPROVED)
                .count();
        
        long rejectedCount = allRequests.stream()
                .filter(h -> h.getStatus() == RequestStatus.REJECTED)
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", pendingCount);
        stats.put("approvedCount", approvedCount);
        stats.put("rejectedCount", rejectedCount);
        stats.put("totalCount", allRequests.size());
        
        return stats;
    }

    /**
     * PersonalHolidayエンティティをレスポンス形式に変換
     */
    private List<AdminHolidayRequestResponse> convertToAdminHolidayRequestResponse(List<PersonalHoliday> holidays) {
        return holidays.stream().map(this::convertToAdminHolidayRequestResponse).collect(Collectors.toList());
    }

    /**
     * 単一のPersonalHolidayエンティティをレスポンス形式に変換
     */
    private AdminHolidayRequestResponse convertToAdminHolidayRequestResponse(PersonalHoliday holiday) {
        AdminHolidayRequestResponse response = new AdminHolidayRequestResponse();
        
        response.setId(holiday.getId());
        response.setUserId(holiday.getUser().getId());
        response.setUserName(holiday.getUser().getUsername());
        response.setStartDate(holiday.getHolidayDate().toString());
        response.setEndDate(holiday.getHolidayDate().toString()); // 単日申請として扱う
        response.setReason(holiday.getReason());
        response.setStatus(holiday.getStatus().name().toLowerCase());
        response.setRequestDate(holiday.getCreatedAt().toString());
        response.setType(holiday.getHolidayType().name().toLowerCase());
        
        // 承認者情報
        if (holiday.getApprover() != null) {
            response.setApproverName(holiday.getApprover().getUsername());
        } else {
            response.setApproverName("-");
        }
        
        return response;
    }
}	