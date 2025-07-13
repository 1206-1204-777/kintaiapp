package com.example.kinntai.repository;

import com.example.kinntai.entity.OvertimeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OvertimeRepository extends JpaRepository<OvertimeRequest, Long> {
    List<OvertimeRequest> findByUserId(Long userId);
}