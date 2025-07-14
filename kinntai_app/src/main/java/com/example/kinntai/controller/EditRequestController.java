package com.example.kinntai.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.config.ApiResponse;
import com.example.kinntai.dto.CorrectionRequestDto;
import com.example.kinntai.entity.EditRequest;
import com.example.kinntai.service.EditRequestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance/edit-requests")
@RequiredArgsConstructor
public class EditRequestController {

    private final EditRequestService editRequestService;

    // 申請作成
    @PostMapping
    public ResponseEntity<ApiResponse> submitRequest(@RequestBody CorrectionRequestDto dto) {
        try {
            editRequestService.submitRequest(dto);
            return ResponseEntity.ok(new ApiResponse("申請を受け付けました。", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("申請に失敗しました: " + e.getMessage(), false));
        }
    }

    // 一般ユーザーの申請一覧取得
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EditRequest>> getUserRequests(@PathVariable Long userId) {
        return ResponseEntity.ok(editRequestService.getRequestsByUser(userId));
    }

    // 申請承認（管理者用）
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approve(@PathVariable Long id, @RequestParam Long approverId) {
        editRequestService.approveRequest(id, approverId);
        return ResponseEntity.ok(new ApiResponse("承認しました。", true));
    }

    // 申請否認（管理者用）
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse> reject(@PathVariable Long id, @RequestParam Long approverId) {
        editRequestService.rejectRequest(id, approverId);
        return ResponseEntity.ok(new ApiResponse("否認しました。", true));
    }
}
