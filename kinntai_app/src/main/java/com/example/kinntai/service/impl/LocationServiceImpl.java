package com.example.kinntai.service.impl;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.LocationRequest;
import com.example.kinntai.entity.Location;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.LocationRepository;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.LocationService;

@Service
public class LocationServiceImpl implements LocationService {

	@Autowired
	private LocationRepository locationRepository;
	@Autowired
	private UserRepository userRepository;

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	/**
	 * 新しい勤務地を作成します
	 */
	@Override
	@Transactional
	public Location createLocation(LocationRequest request) {
		if (request.getName() == null || request.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("勤務地名は必須です");
		}
		
		Optional<User> user = userRepository.findByUsername(request.getName());

		LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
		LocalTime endTime = LocalTime.parse(request.getEndTime(), TIME_FORMATTER);

		Location location = new Location();
		location.setName(request.getName());
		location.setCreatedBy(request.getCreatedBy());
		location.setStartTime(startTime);
		location.setEndTime(endTime);
		return locationRepository.save(location);
	}

	/**
	 * IDで勤務地を検索します
	 */
	@Override
	public Optional<Location> findById(Long id) {
		return locationRepository.findById(id);
	}

	/**
	 * すべての勤務地を取得します
	 */
	@Override
	public List<Location> findAll() {
		return locationRepository.findAll();
	}

	/**
	 * 勤務地を更新します
	 */
	@Override
	@Transactional
	public Location updateLocation(Long id, LocationRequest request) {
		Location location = locationRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("勤務地が見つかりません: " + id));

		if (request.getName() != null && !request.getName().trim().isEmpty()) {
			// 名前を変更する場合の重複チェック
			if (!request.getName().equals(location.getName())
					&& locationRepository.findByName(request.getName()).isPresent()) {
				throw new IllegalArgumentException("この勤務地名は既に存在します。");
			}
			location.setName(request.getName());
		}

		if (request.getStartTime() != null && !request.getStartTime().trim().isEmpty()) {
			location.setStartTime(LocalTime.parse(request.getStartTime(), TIME_FORMATTER));
		}

		if (request.getEndTime() != null && !request.getEndTime().trim().isEmpty()) {
			location.setEndTime(LocalTime.parse(request.getEndTime(), TIME_FORMATTER));
		}

		return locationRepository.save(location);
	}

	/**
	 * 勤務地を削除します
	 */
	@Override
	@Transactional
	public boolean deleteLocation(Long id) {
		if (locationRepository.existsById(id)) {
			locationRepository.deleteById(id);
			return true;
		}
		return false;
	}
}