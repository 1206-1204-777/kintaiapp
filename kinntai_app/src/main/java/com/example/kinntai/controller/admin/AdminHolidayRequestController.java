package com.example.kinntai.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.admin.AdminHolidayRequestResponse;
import com.example.kinntai.dto.admin.HolidayRequestActionRequest;
import com.example.kinntai.service.admin.AdminHolidayRequestService;

/**
 * 管理者向け休暇申請管理APIコントローラー
 * 全社員の休暇申請の確認、承認・却下処理を提供
 */
@RestController
@RequestMapping("/api/admin/holiday-requests")
@CrossOrigin(origins = "*")
public class AdminHolidayRequestController {

    @Autowired
    private AdminHolidayRequestService adminHolidayRequestService;

    /**
     * 全ての休暇申請を取得
     * ステータスやユーザーでのフィルタリングが可能
     *
     * @param status 申請ステータス（pending/approved/rejected）
     * @param userId ユーザーID（特定ユーザーの申請のみ取得する場合）
     * @return 休暇申請一覧
     */
    @GetMapping
    public ResponseEntity<List<AdminHolidayRequestResponse>> getAllHolidayRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        try {
            List<AdminHolidayRequestResponse> requests = adminHolidayRequestService.getAllHolidayRequests(status, userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            System.err.println("休暇申請一覧取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 承認待ちの休暇申請のみを取得
     * 管理者のダッシュボードで表示するために使用
     *
     * @return 承認待ちの休暇申請一覧
     */
    @GetMapping("/pending")
    public ResponseEntity<List<AdminHolidayRequestResponse>> getPendingHolidayRequests() {
        try {
            List<AdminHolidayRequestResponse> pendingRequests = adminHolidayRequestService.getPendingHolidayRequests();
            return ResponseEntity.ok(pendingRequests);
        } catch (Exception e) {
            System.err.println("承認待ち休暇申請取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 特定の休暇申請の詳細を取得
     *
     * @param requestId 申請ID
     * @return 休暇申請の詳細情報
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<AdminHolidayRequestResponse> getHolidayRequestById(@PathVariable Long requestId) {
        try {
            AdminHolidayRequestResponse request = adminHolidayRequestService.getHolidayRequestById(requestId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("休暇申請詳細取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 休暇申請を承認
     *
     * @param requestId 申請ID
     * @param actionRequest 承認処理の詳細（承認者ID、コメントなど）
     * @return 処理結果
     */
    @PutMapping("/{requestId}/approve")
    public ResponseEntity<?> approveHolidayRequest(
            @PathVariable Long requestId,
            @RequestBody HolidayRequestActionRequest actionRequest) {
        try {
            adminHolidayRequestService.approveHolidayRequest(requestId, actionRequest);
            return ResponseEntity.ok().body("休暇申請を承認しました");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("休暇申請承認エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("休暇申請の承認に失敗しました");
        }
    }

    /**
     * 休暇申請を却下
     *
     * @param requestId 申請ID
     * @param actionRequest 却下処理の詳細（却下者ID、却下理由など）
     * @return 処理結果
     */
    @PutMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectHolidayRequest(
            @PathVariable Long requestId,
            @RequestBody HolidayRequestActionRequest actionRequest) {
        try {
            adminHolidayRequestService.rejectHolidayRequest(requestId, actionRequest);
            return ResponseEntity.ok().body("休暇申請を却下しました");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("休暇申請却下エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("休暇申請の却下に失敗しました");
        }
    }

    /**
     * 休暇申請の統計情報を取得
     * ダッシュボード表示用
     *
     * @return 休暇申請の統計情報
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getHolidayRequestStats() {
        try {
            var stats = adminHolidayRequestService.getHolidayRequestStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("休暇申請統計取得エラー: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}