package com.example.kinntai.service;

import java.util.List;

import com.example.kinntai.dto.CorrectionRequestDto;
import com.example.kinntai.entity.EditRequest;

public interface EditRequestService {
    void submitRequest(CorrectionRequestDto dto);
    List<EditRequest> getRequestsByUser(Long userId);
    void approveRequest(Long requestId, Long approverId);
    void rejectRequest(Long requestId, Long approverId);
}
