package com.example.kinntai.service.impl;

import com.example.kinntai.dto.OvertimeRequestDto;
import com.example.kinntai.entity.OvertimeRequest;
import com.example.kinntai.entity.RequestStatus;
import com.example.kinntai.repository.OvertimeRepository;
import com.example.kinntai.service.OvertimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
}