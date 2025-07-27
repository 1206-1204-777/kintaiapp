package com.example.kinntai.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.kinntai.dto.CorrectionRequestDto;
import com.example.kinntai.entity.Attendance;
import com.example.kinntai.entity.EditRequest;
import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.repository.AttendanceRepository;
import com.example.kinntai.repository.EditRequestRepository;
import com.example.kinntai.service.EditRequestService;
import com.example.kinntai.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EditRequestServiceImpl implements EditRequestService{

    private final EditRequestRepository editRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserService userService;

    public void submitRequest(CorrectionRequestDto dto) {
        // Attendanceの現在時刻を取得
        List<Attendance> records = attendanceRepository.findAllByUser_IdAndDate(dto.getUserId(), dto.getTargetDate());

        Attendance attendance = records.stream()
                .max(Comparator.comparing(Attendance::getCreatedAt))
                .orElse(null);

        EditRequest request = new EditRequest();
        request.setUserId(dto.getUserId());
        request.setTargetDate(dto.getTargetDate());
        request.setRequestedClockIn(dto.getRequestedClockIn());
        request.setRequestedClockOut(dto.getRequestedClockOut());
        request.setReason(dto.getReason());
        request.setStatus(RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());

        if (attendance != null) {
            request.setCurrentClockIn(attendance.getClockIn() != null ? attendance.getClockIn().toLocalTime() : null);
            request.setCurrentClockOut(attendance.getClockOut() != null ? attendance.getClockOut().toLocalTime() : null);
        }

        editRequestRepository.save(request);
    }

    public List<EditRequest> getRequestsByUser(Long userId) {
        return editRequestRepository.findByUserId(userId);
    }

    public void approveRequest(Long requestId, Long approverId) {
        EditRequest request = editRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("申請が見つかりません"));

        request.setStatus(RequestStatus.APPROVED);
        request.setApproverId(approverId);
        request.setApprovedDate(LocalDateTime.now());

        // Attendanceの修正
        Attendance attendance = attendanceRepository.findAllByUser_IdAndDate(
                        request.getUserId(), request.getTargetDate()).stream()
                .max(Comparator.comparing(Attendance::getCreatedAt))
                .orElseThrow(() -> new IllegalArgumentException("該当勤怠が見つかりません"));

        if (request.getRequestedClockIn() != null) {
            attendance.setClockIn(request.getTargetDate().atTime(request.getRequestedClockIn()));
        }
        if (request.getRequestedClockOut() != null) {
            attendance.setClockOut(request.getTargetDate().atTime(request.getRequestedClockOut()));
        }

        attendance.setUpdatedAt(LocalDateTime.now());

        attendanceRepository.save(attendance);
        editRequestRepository.save(request);
    }

    public void rejectRequest(Long requestId, Long approverId) {
        EditRequest request = editRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("申請が見つかりません"));

        request.setStatus(RequestStatus.REJECTED);
        request.setApproverId(approverId);
        request.setApprovedDate(LocalDateTime.now());

        editRequestRepository.save(request);
    }

    // 管理者用: 全ユーザーの勤怠修正申請を取得
    public List<EditRequest> getAllEditRequests() {
        return editRequestRepository.findAllByOrderByRequestDateDesc();
    }

    // 管理者用: 承認待ちの勤怠修正申請数を取得
    public int getPendingEditRequestCount() {
        return editRequestRepository.countByStatus(RequestStatus.PENDING);
    }

    // 管理者用: 全勤怠修正申請数を取得
    public int getTotalRequestCount() {
        return (int) editRequestRepository.count();
    }

    // 管理者用: ステータス別勤怠修正申請を取得
    public List<EditRequest> getEditRequestsByStatus(RequestStatus status) {
        return editRequestRepository.findByStatus(status);
    }
}