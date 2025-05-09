package com.example.kinntai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.LoginRequest;
import com.example.kinntai.dto.SignupRequest;
import com.example.kinntai.dto.UserResponse;
import com.example.kinntai.service.impl.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @PostMapping("/signup")
    @Transactional
    public UserResponse registerUser(@RequestBody SignupRequest request) {
        // ユーザー名の重複チェック
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new RuntimeException("ユーザー名は必須です");
        }
        
        // 新しいユーザーを作成
        return authService.registerUser(request);
    }
    
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        try {
            UserResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}