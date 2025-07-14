package com.example.kinntai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kinntai.entity.EditRequest;

public interface EditRequestRepository extends JpaRepository<EditRequest, Long> {
    List<EditRequest> findByUserId(Long userId);
}
