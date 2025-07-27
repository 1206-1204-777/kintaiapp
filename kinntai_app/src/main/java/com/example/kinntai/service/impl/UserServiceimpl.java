package com.example.kinntai.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.UserLocationResponse;
import com.example.kinntai.entity.Location;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.LocationRepository;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.UserService;

@Service
public class UserServiceimpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private LocationRepository locationRepository;

	/**
	 * ユーザー名でユーザーを検索します
	 */
	@Override
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	/**
	 * IDでユーザーを検索します
	 */
	@Override
	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	/**
	 * すべてのユーザーを取得します
	 */
	@Override
	public List<User> findAll() {
		return userRepository.findAll();
	}

	/**
	 * ユーザーに勤務地を割り当てます
	 */
	@Override
	@Transactional
	public User assignLocationToUser(User user, Location location) {
		user.setLocation(location);
		return userRepository.save(user);
	}

	/**
	 * ユーザー情報を更新します
	 */
	@Override
	@Transactional
	public User updateUser(Long id, User userDetails) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + id));

		if (userDetails.getUsername() != null && !userDetails.getUsername().trim().isEmpty()) {
			user.setUsername(userDetails.getUsername());
		}

		return userRepository.save(user);
	}

	/**
	 * ユーザーを削除します
	 */
	@Override
	@Transactional
	public boolean deleteUser(Long id) {
		if (userRepository.existsById(id)) {
			userRepository.deleteById(id);
			return true;
		}
		return false;
	}

	/**
	 * ユーザーの勤務地情報を取得
	 */
	@Override
	public UserLocationResponse getUserLocationInfo(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

		
 
		UserLocationResponse response = new UserLocationResponse();
		response.setUserId(user.getId());
		response.setUsername(user.getUsername());
		response.setStartTime(user.getDefaultStartTime());
		response.setEndTime(user.getDefaultEndTime());

		if (user.getLocation() != null) {
			Location location = user.getLocation();
			response.setLocationId(location.getId());
			response.setLocationName(location.getName());
			response.setStartTime(user.getDefaultStartTime());
			response.setEndTime(user.getDefaultEndTime());
		}

		return response;
	}

}