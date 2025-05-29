package com.example.kinntai.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.PersonalHolidayRequest;
import com.example.kinntai.dto.PersonalHolidayResponse;
import com.example.kinntai.entity.PersonalHoliday;
import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.service.PersonalHolidayService;
import com.example.kinntai.service.UserService;

@RestController
@RequestMapping("/api/personal-holidays")
@CrossOrigin(origins = "*")
/**
 * 個人休日に関する操作を提供するコントローラークラスです。
 * ユーザーの個人休日申請、取得、更新、削除などの機能を提供します。
 */
public class PersonalHolidayController {

    @Autowired
    private PersonalHolidayService personalHolidayService;

    @Autowired
    private UserService userService;

    /**
     * 個人休日を申請するエンドポイントです。
     *
     * @param request 申請内容を含むリクエスト
     * @return 登録された個人休日の情報またはエラーメッセージ
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyPersonalHoliday(@RequestBody PersonalHolidayRequest request) {
        try {
            PersonalHoliday personalHoliday = personalHolidayService.applyPersonalHoliday(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToPersonalHolidayResponse(personalHoliday));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("個人休日申請に失敗しました: " + e.getMessage());
        }
    }

    /**
     * 指定ユーザーの個人休日申請一覧を取得します。
     *
     * @param userId ユーザーID
     * @return 個人休日のレスポンスリスト
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PersonalHolidayResponse>> getPersonalHolidaysByUserId(@PathVariable Long userId) {
        List<PersonalHoliday> holidays = personalHolidayService.getPersonalHolidaysByUserId(userId);
        List<PersonalHolidayResponse> responses = holidays.stream()
                .map(this::convertToPersonalHolidayResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 個人休日申請のステータスを更新します（管理者用）。
     *
     * @param id 申請ID
     * @param status 新しいステータス
     * @param approverId 承認者のユーザーID
     * @return 更新後の個人休日レスポンス
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePersonalHolidayStatus(@PathVariable Long id,
                                                         @RequestParam String status,
                                                         @RequestParam Long approverId) {
        try {
            RequestStatus newStatus = RequestStatus.valueOf(status.toUpperCase());
            PersonalHoliday updatedHoliday = personalHolidayService.updatePersonalHolidayStatus(id, newStatus, approverId);
            return ResponseEntity.ok(convertToPersonalHolidayResponse(updatedHoliday));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("個人休日申請ステータス更新に失敗しました: " + e.getMessage());
        }
    }

    /**
     * 個人休日申請をキャンセルします（ユーザー用）。
     *
     * @param id 個人休日申請ID
     * @param userId ユーザーID
     * @return 成功または失敗メッセージ
     */
    @DeleteMapping("/{id}/cancel/user/{userId}")
    public ResponseEntity<?> cancelPersonalHoliday(@PathVariable Long id, @PathVariable Long userId) {
        try {
            boolean cancelled = personalHolidayService.cancelPersonalHoliday(id, userId);
            if (cancelled) {
                return ResponseEntity.ok("個人休日申請がキャンセルされました。");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("指定された個人休日申請が見つからないか、キャンセルできませんでした。");
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("個人休日申請のキャンセルに失敗しました: " + e.getMessage());
        }
    }

    /**
     * PersonalHolidayエンティティをレスポンスDTOに変換します。
     *
     * @param personalHoliday エンティティ
     * @return レスポンスDTO
     */
    private PersonalHolidayResponse convertToPersonalHolidayResponse(PersonalHoliday personalHoliday) {
        PersonalHolidayResponse response = new PersonalHolidayResponse();
        response.setId(personalHoliday.getId());
        response.setUserId(personalHoliday.getUser().getId());
        response.setUsername(personalHoliday.getUser().getUsername());
        response.setHolidayDate(personalHoliday.getHolidayDate());
        response.setHolidayType(personalHoliday.getHolidayType().name());
        response.setReason(personalHoliday.getReason());
        response.setStatus(personalHoliday.getStatus().name());
        response.setApproverName(personalHoliday.getApprover() != null
                ? personalHoliday.getApprover().getUsername()
                : "-");
        response.setCreatedAt(personalHoliday.getCreatedAt());
        response.setUpdatedAt(personalHoliday.getUpdatedAt());
        return response;
    }
}
