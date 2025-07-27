package com.example.kinntai.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.kinntai.dto.OvertimeRequestDto;
import com.example.kinntai.entity.OvertimeRequest;
import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.repository.OvertimeRepository;
import com.example.kinntai.service.OvertimeService;

@Service
public class OvertimeServiceImpl implements OvertimeService {

    @Autowired
    private OvertimeRepository repository;

    @Override
    public OvertimeRequest submitOvertimeRequest(OvertimeRequestDto dto) {
        OvertimeRequest request = new OvertimeRequest();
        request.setUserId(dto.getUserId());
        request.setRequestDate(dto.getRequestDate());
        request.setOvertimeMinutes(dto.getOvertimeMinutes());
        request.setReason(dto.getReason());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        return repository.save(request);
    }

    @Override
    public List<OvertimeRequest> getOvertimeRequestsByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public OvertimeRequest approveOvertime(Long id) {
        OvertimeRequest request = repository.findById(id).orElseThrow();
        request.setStatus(RequestStatus.APPROVED);
        request.setUpdatedAt(LocalDateTime.now());
        return repository.save(request);
    }

    @Override
    public OvertimeRequest rejectOvertime(Long id) {
        OvertimeRequest request = repository.findById(id).orElseThrow();
        request.setStatus(RequestStatus.REJECTED);
        request.setUpdatedAt(LocalDateTime.now());
        return repository.save(request);
    }
    
 // OvertimeServiceImpl.java に追加するメソッド
 // 既存のクラスに以下のメソッドを追加してください

 /**
  * 管理者用: 全ユーザーの残業申請を取得
  */
 public List<OvertimeRequest> getAllOvertimeRequests() {
     return repository.findAllByOrderByCreatedAtDesc();
 }

 /**
  * 管理者用: 承認待ちの残業申請数を取得
  */
 public int getPendingOvertimeRequestCount() {
     return repository.countByStatus(RequestStatus.PENDING);
 }

 /**
  * 管理者用: 全残業申請数を取得
  */
 public int getTotalRequestCount() {
     return (int) repository.count();
 }

 /**
  * 管理者用: ステータス別残業申請を取得
  */
 public List<OvertimeRequest> getOvertimeRequestsByStatus(RequestStatus status) {
     return repository.findByStatus(status);
 }
}