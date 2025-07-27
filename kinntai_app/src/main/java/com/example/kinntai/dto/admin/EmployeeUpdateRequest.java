package com.example.kinntai.dto.admin;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.kinntai.entity.UserRole;

/**
 * 従業員情報更新用のリクエストDTO
 */
public record EmployeeUpdateRequest(
    String username,
    String email,
    UserRole role,
    Long locationId,
    LocalTime defaultStartTime,
    LocalTime defaultEndTime,
    LocalDate hireDate
) {}
