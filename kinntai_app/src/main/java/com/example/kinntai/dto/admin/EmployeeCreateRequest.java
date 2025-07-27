package com.example.kinntai.dto.admin;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.kinntai.entity.UserRole;

/**
 * 新規従業員登録用のリクエストDTO
 */
public record EmployeeCreateRequest(
    String username,
    String email,
    String password,
    UserRole role,
    Long locationId,
    LocalTime defaultStartTime,
    LocalTime defaultEndTime,
    LocalDate hireDate
) {}
