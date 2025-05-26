package com.example.kinntai.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.LoginRequest;
import com.example.kinntai.dto.SignupRequest;
import com.example.kinntai.dto.UserResponse;
import com.example.kinntai.entity.Location;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.UserRole;
import com.example.kinntai.repository.LocationRepository;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private LocationRepository locationRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public UserResponse registerUser(SignupRequest request) {
		// ユーザー名の重複チェック
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new RuntimeException("このユーザー名は既に使用されています");
		}

		// パスワードのハッシュ化
		String encodedPassword = passwordEncoder.encode(request.getPassword());

		// 新しいユーザーを作成と保存
		User user = new User();
		Location location = new Location();
		location.setName("55555555");
		location.setStartTime(request.getStartTime());
		location.setEndTime(request.getEndTime());
		location.setRole(UserRole.USER);
		
		locationRepository.save(location);
		
		
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPassword(encodedPassword); // BCryptでエンコードされたパスワード
		user.setRole(UserRole.USER);
		user.setLocation(location);

		if (request.getStartTime() != null || request.getEndTime() != null) {
			user.setDefaultStartTime((request.getStartTime()));
			user.setDefaultEndTime(request.getEndTime());
		}
		User savedUser = userRepository.save(user);

		// レスポンスの作成
		UserResponse response = new UserResponse();
		response.setUserId(savedUser.getId());
		response.setUsername(savedUser.getUsername());
		response.setEmail(savedUser.getEmail());
		response.setStartTime(savedUser.getDefaultStartTime());
		response.setEndTime(savedUser.getDefaultEndTime());
		response.setToken("dummy-token-" + savedUser.getId());

		// 勤務地情報が設定されている場合はそれも含める
		if (savedUser.getLocation() != null) {
			response.setLocationId(savedUser.getLocation().getId());
			response.setLocationName(savedUser.getLocation().getName());
		}

		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse login(LoginRequest request) {
		try {
			// ユーザーを検索
			User user = userRepository.findByUsername(request.getUsername())
					.orElseThrow(() -> new RuntimeException("ユーザー名またはパスワードが正しくありません"));

			// パスワードの検証 - 開発中はコメントアウトして単純化
			// if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			//     throw new RuntimeException("ユーザー名またはパスワードが正しくありません");
			// }

			// レスポンスの作成
			UserResponse response = new UserResponse();
			response.setUserId(user.getId());
			response.setUsername(user.getUsername());
			response.setEmail(user.getEmail());
			response.setToken("dummy-token-" + user.getId());

			// 勤務地情報が設定されている場合はそれも含める
			if (user.getLocation() != null) {
				response.setLocationId(user.getLocation().getId());
				response.setLocationName(user.getLocation().getName());
			}

			return response;
		} catch (Exception e) {
			System.err.println("ログインエラー: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}