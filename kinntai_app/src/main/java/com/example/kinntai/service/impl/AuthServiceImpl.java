package com.example.kinntai.service.impl;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.LoginRequest;
import com.example.kinntai.dto.SignupRequest;
import com.example.kinntai.dto.UserResponse;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.UserRole;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.AuthService;
import com.example.kinntai.util.JwtUtil;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtUtil jwtUtil;

	@Override
	@Transactional
	public UserResponse registerUser(@Valid SignupRequest request) {
		// ユーザー名の重複チェック
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new RuntimeException("このユーザー名は既に使用されています");
		}

		// パスワードのハッシュ化
		String encodedPassword = passwordEncoder.encode(request.getPassword());
		UserRole role = toUserRoleOrDefault(request.getRole().toString(), UserRole.GENERAL_USER);

		// 新しいユーザーを作成と保存
		User newUser = User.builder()
				.username(request.getUsername())
				.email(request.getEmail())
				.password(encodedPassword)
				.role(role)
				.build();

		User savedUser = userRepository.save(newUser);
		/*登録後に自動ログイン
		 * トークンを返す*/
		String jwt = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole().name());

		return UserResponse.builder()
				.userId(savedUser.getId())
				.username(savedUser.getUsername())
				.email(savedUser.getEmail())
				.role(savedUser.getRole())
				.token(jwt)
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse login(LoginRequest request) {
		try {

			/*トークン認証*/
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);//認証に成功したらコンテキスト（設定）に格納

			UserDetails principal = (UserDetails) authentication.getPrincipal();

			User userDetails = userRepository.findByUsername(principal.getUsername())
					.orElseThrow(() -> new RuntimeException("認証されたユーザーが見つかりません。"));

			/*jwtトークン作成*/
			String jwt = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getRole().name());//トークン発行
			return UserResponse.builder()
					.userId(userDetails.getId())
					.username(userDetails.getUsername())
					.email(userDetails.getEmail())
					.role(userDetails.getRole())
					.token(jwt)
					.build();
		} catch (Exception e) {
			log.error("ログインエラー: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	private static UserRole toUserRoleOrDefault(String raw, UserRole def) {
		if (raw == null || raw.isBlank())
			return def;
		try {
			return UserRole.valueOf(raw.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return def;
		}
	}
}