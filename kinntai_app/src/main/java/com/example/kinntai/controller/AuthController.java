package com.example.kinntai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserResponse> registerUser(@RequestBody SignupRequest request) {
        try {
            System.out.println("ユーザー登録リクエスト: " + request.getUsername());
            
            // ユーザー名の重複チェック
            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                throw new RuntimeException("ユーザー名は必須です");
            }

            // 新しいユーザーを作成
            UserResponse response = authService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ユーザー登録エラー: " + e.getMessage());
            e.printStackTrace();
            throw e; // エラーを再スロー - クライアントに詳細エラーを返すため
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("ログインリクエスト: " + request.getUsername());
            UserResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ログインエラー: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}