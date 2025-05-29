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
import com.example.kinntai.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
/**
 * 認証処理を担当するコントローラークラスです。
 * ユーザーのサインアップおよびログイン機能を提供します。
 */
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 新規ユーザー登録（サインアップ）を処理するエンドポイントです。
     *
     * @param request ユーザーの登録情報
     * @return 登録されたユーザー情報
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> registerUser(@RequestBody SignupRequest request) {
        try {
            System.out.println("ユーザー登録リクエスト: " + request.getUsername());

            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                throw new RuntimeException("ユーザー名は必須です");
            }

            UserResponse response = authService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ユーザー登録エラー: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * ユーザーログインを処理するエンドポイントです。
     *
     * @param request ログイン情報
     * @return 認証されたユーザー情報またはエラーメッセージ
     */
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
