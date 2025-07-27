package com.example.kinntai.dto.admin;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.kinntai.entity.User;

/**
 * 従業員一覧表示用のDTO
 */
public record EmployeeDto(
    Long id,
    String name,
    String email,
    String departmentName,
    String locationName,
    LocalTime defaultStartTime,
    LocalTime defaultEndTime,
    LocalDate hireDate,
    String role
) {
    public static EmployeeDto fromEntity(User user) {
        // TODO: Userエンティティに hireDate フィールドを追加後、コメントを解除してください。
        // LocalDate hire_date = user.getHireDate();
        LocalDate hire_date = LocalDate.now(); // 仮のデータ

        // TODO: LocationエンティティとDepartmentエンティティの関連付けを確認後、修正してください。
        String department_name = "部署未設定";
        if (user.getLocation() != null) {
            // department_name = user.getLocation().getDepartment().getName(); // 本来の形
        }

        return new EmployeeDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            department_name, // 仮のデータ
            user.getLocation() != null ? user.getLocation().getName() : "勤務地未設定",
            user.getDefaultStartTime(),
            user.getDefaultEndTime(),
            hire_date,
            user.getRole().name()
        );
    }
}
