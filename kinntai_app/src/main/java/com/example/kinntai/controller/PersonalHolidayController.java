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
import com.example.kinntai.entity.RequestStatus; // RequestStatus Enumを使用
import com.example.kinntai.service.PersonalHolidayService;
import com.example.kinntai.service.UserService; // ユーザー情報を取得するサービス

@RestController
@RequestMapping("/api/personal-holidays")
@CrossOrigin(origins = "*") // CORS対応
public class PersonalHolidayController {

    @Autowired
    private PersonalHolidayService personalHolidayService;

    @Autowired
    private UserService userService; // 申請者や承認者のユーザー名を取得するため

    // 個人休日の申請
    @PostMapping("/apply")
    public ResponseEntity<?> applyPersonalHoliday(@RequestBody PersonalHolidayRequest request) {
        try {
            PersonalHoliday personalHoliday = personalHolidayService.applyPersonalHoliday(request);
            // レスポンスDTOに変換して返す
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToPersonalHolidayResponse(personalHoliday));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("個人休日申請に失敗しました: " + e.getMessage());
        }
    }

    // 特定のユーザーの個人休日申請一覧を取得
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PersonalHolidayResponse>> getPersonalHolidaysByUserId(@PathVariable Long userId) {
        List<PersonalHoliday> holidays = personalHolidayService.getPersonalHolidaysByUserId(userId);
        List<PersonalHolidayResponse> responses = holidays.stream()
                .map(this::convertToPersonalHolidayResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // 個人休日申請のステータス更新 (管理者向けAPI)
    // 例: /api/personal-holidays/{id}/status?status=APPROVED&approverId=1
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePersonalHolidayStatus(
            @PathVariable Long id,
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

    // 個人休日申請のキャンセル (ユーザー自身が申請中のものをキャンセル)
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

    // PersonalHoliday エンティティを PersonalHolidayResponse DTO に変換するヘルパーメソッド
    private PersonalHolidayResponse convertToPersonalHolidayResponse(PersonalHoliday personalHoliday) {
        PersonalHolidayResponse response = new PersonalHolidayResponse();
        response.setId(personalHoliday.getId());
        response.setUserId(personalHoliday.getUser().getId());
        response.setUsername(personalHoliday.getUser().getUsername()); // 申請者名
        response.setHolidayDate(personalHoliday.getHolidayDate());
        response.setHolidayType(personalHoliday.getHolidayType().name()); // EnumをStringに変換
        response.setReason(personalHoliday.getReason());
        response.setStatus(personalHoliday.getStatus().name()); // EnumをStringに変換
        if (personalHoliday.getApprover() != null) {
            response.setApproverName(personalHoliday.getApprover().getUsername()); // 承認者名
        } else {
            response.setApproverName("-");
        }
        response.setCreatedAt(personalHoliday.getCreatedAt());
        response.setUpdatedAt(personalHoliday.getUpdatedAt());
        return response;
    }
}