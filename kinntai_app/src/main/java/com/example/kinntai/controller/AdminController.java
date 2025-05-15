package com.example.kinntai.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.AdminRegister;
import com.example.kinntai.dto.AttendanceResponse;
import com.example.kinntai.service.AdminService;
import com.example.kinntai.service.impl.AttendanceService;

@RestController
@RequestMapping("/api")  // ベースパスを/adminから/apiに変更
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private AdminService service;
    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/auth/admin/signup")  // /admin/signupから/api/auth/admin/signupに変更
    public ResponseEntity<String> registerAdmin(@RequestBody AdminRegister dto) {
        if (dto == null) {
            throw new RuntimeException("値が入力されていません。");
        }

        service.registerAdmin(dto);
        return ResponseEntity.ok("管理者登録が完了しました。");
    }

    /*全ユーザーの勤怠一覧*/
    @GetMapping("/admin/attendance")  // /admin/Attendanceから/api/admin/attendanceに変更（小文字に修正）
    public List<AttendanceResponse> getAllUser() throws RuntimeException {
        return attendanceService.getAllUser();
    }
}