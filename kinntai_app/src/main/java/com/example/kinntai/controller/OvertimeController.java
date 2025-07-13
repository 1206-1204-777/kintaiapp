package com.example.kinntai.controller;

import com.example.kinntai.dto.OvertimeRequestDto;
import com.example.kinntai.entity.OvertimeRequest;
import com.example.kinntai.service.OvertimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/overtime")
public class OvertimeController {

    @Autowired
    private OvertimeService overtimeService;

    @PostMapping
    public OvertimeRequest submitRequest(@RequestBody OvertimeRequestDto dto) {
        return overtimeService.submitOvertimeRequest(dto);
    }

    @GetMapping
    public List<OvertimeRequest> getUserRequests(@RequestParam Long userId) {
        return overtimeService.getOvertimeRequestsByUser(userId);
    }

    @PutMapping("/{id}/approve")
    public OvertimeRequest approve(@PathVariable Long id) {
        return overtimeService.approveOvertime(id);
    }

    @PutMapping("/{id}/reject")
    public OvertimeRequest reject(@PathVariable Long id) {
        return overtimeService.rejectOvertime(id);
    }
}