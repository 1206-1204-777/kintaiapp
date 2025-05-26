package com.example.kinntai.service;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.LocationRequest;
import com.example.kinntai.entity.Location;

public interface LocationService {

	/**
	 * 新しい勤務地を作成します
	 */
	Location createLocation(LocationRequest request);

	/**
	 * IDで勤務地を検索します
	 */
	Optional<Location> findById(Long id);

	/**
	 * すべての勤務地を取得します
	 */
	List<Location> findAll();

	/**
	 * 勤務地を更新します
	 */
	Location updateLocation(Long id, LocationRequest request);

	/**
	 * 勤務地を削除します
	 */
	boolean deleteLocation(Long id);

}