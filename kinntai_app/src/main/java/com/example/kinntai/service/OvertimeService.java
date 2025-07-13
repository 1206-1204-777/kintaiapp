package com.example.kinntai.service;

import com.example.kinntai.dto.OvertimeRequestDto;
import com.example.kinntai.entity.OvertimeRequest;

import java.util.List;

public interface OvertimeService {
    OvertimeRequest submitOvertimeRequest(OvertimeRequestDto dto);
    List<OvertimeRequest> getOvertimeRequestsByUser(Long userId);
    OvertimeRequest approveOvertime(Long id);
    OvertimeRequest rejectOvertime(Long id);
}