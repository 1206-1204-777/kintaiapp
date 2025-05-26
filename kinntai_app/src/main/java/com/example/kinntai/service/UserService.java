package com.example.kinntai.service;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.UserLocationResponse;
import com.example.kinntai.entity.Location;
import com.example.kinntai.entity.User;

public interface UserService {

	/**
	 * ユーザー名でユーザーを検索します
	 */
	Optional<User> findByUsername(String username);

	/**
	 * IDでユーザーを検索します
	 */
	Optional<User> findById(Long id);

	/**
	 * すべてのユーザーを取得します
	 */
	List<User> findAll();

	/**
	 * ユーザーに勤務地を割り当てます
	 */
	User assignLocationToUser(User user, Location location);

	/**
	 * ユーザー情報を更新します
	 */
	User updateUser(Long id, User userDetails);

	/**
	 * ユーザーを削除します
	 */
	boolean deleteUser(Long id);

	/**
	 * ユーザーの勤務地情報を取得
	 */
	UserLocationResponse getUserLocationInfo(Long userId);

}