package com.example.kinntai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kinntai.entity.OvertimeRequest;
import com.example.kinntai.entity.RequestStatus;

public interface OvertimeRepository extends JpaRepository<OvertimeRequest, Long> {
    List<OvertimeRequest> findByUserId(Long userId);
    List<OvertimeRequest> findByStatus(RequestStatus status);
    int countByStatus(RequestStatus status);
    List<OvertimeRequest> findAllByOrderByCreatedAtDesc();
}