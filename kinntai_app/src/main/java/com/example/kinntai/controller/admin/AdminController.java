package com.example.kinntai.controller.admin;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.admin.EmployeeCreateRequest;
import com.example.kinntai.dto.admin.EmployeeDto;
import com.example.kinntai.dto.admin.EmployeeUpdateRequest;
import com.example.kinntai.service.admin.AdminService;


@RestController
@RequestMapping("/api/admin") // 管理者用APIのベースURL
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 全従業員の一覧を取得
     */
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> employees = adminService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    /**
     * 新規従業員を登録
     */
    @PostMapping("/employees")
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody EmployeeCreateRequest request) {
        try {
            EmployeeDto createdEmployee = adminService.createEmployee(request);
            return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // ユーザー名・Email重複
        }
    }

    /**
     * 従業員情報を更新
     */
    @PutMapping("/employees/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @RequestBody EmployeeUpdateRequest request) {
        try {
            EmployeeDto updatedEmployee = adminService.updateEmployee(id, request);
            return ResponseEntity.ok(updatedEmployee);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 従業員を削除
     */
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        adminService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // 他にも、統計情報取得、申請一覧取得、申請ステータス更新などのエンドポイントをここに追加します。
    // 例:
    // @GetMapping("/stats")
    // @GetMapping("/requests")
    // @PostMapping("/requests/{requestId}/status")
}
