package com.example.kinntai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kinntai.entity.EditRequest;
import com.example.kinntai.entity.RequestStatus;

public interface EditRequestRepository extends JpaRepository<EditRequest, Long> {
    List<EditRequest> findByUserId(Long userId);
    List<EditRequest> findByStatus(RequestStatus status);
    int countByStatus(RequestStatus status);
    List<EditRequest> findAllByOrderByRequestDateDesc();
}