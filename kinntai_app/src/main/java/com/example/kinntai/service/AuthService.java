package com.example.kinntai.service;

import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.LoginRequest;
import com.example.kinntai.dto.SignupRequest;
import com.example.kinntai.dto.UserResponse;

public interface AuthService {

	UserResponse registerUser(SignupRequest request);

	UserResponse login(LoginRequest request);

}