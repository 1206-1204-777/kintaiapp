package com.example.kinntai.dto;

// 登録リクエスト用DTO
public class SignupRequest {
    private String username;
    private String password;
    private String fullName;
    
    // ゲッターとセッター
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}