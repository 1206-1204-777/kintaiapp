package com.example.kinntai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.UserLocationResponse;
import com.example.kinntai.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
/**
 * ユーザー情報に関する操作を提供するコントローラークラスです。
 * ユーザーの基本情報や位置情報の取得機能を提供します。
 */
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 指定されたユーザーIDに基づいてユーザー情報を取得します。
     *
     * @param userId ユーザーID
     * @return ユーザー情報または404エラー
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        try {
            return userService.findById(userId)
                    .map(user -> ResponseEntity.ok(user))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 指定されたユーザーIDの位置情報を取得します。
     *
     * @param userId ユーザーID
     * @return ユーザーの位置情報
     */
    @GetMapping("/{userId}/location")
    public ResponseEntity<UserLocationResponse> getUserLocation(@PathVariable Long userId) {
        try {
            UserLocationResponse response = userService.getUserLocationInfo(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 指定されたユーザーIDの詳細情報を取得します。
     *
     * @param userId ユーザーID
     * @return 詳細なユーザー位置情報
     */
    @GetMapping("/{userId}/info")
    public ResponseEntity<UserLocationResponse> getUserInfoDetails(@PathVariable Long userId) {
        UserLocationResponse response = userService.getUserLocationInfo(userId);
        return ResponseEntity.ok(response);
    }
}
