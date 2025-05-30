package com.example.kinntai.controller;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.LocationRequest;
import com.example.kinntai.entity.Location;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.service.LocationService;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
/**
 * 勤務地情報に関する操作を提供するコントローラークラスです。
 * 勤務地の登録、取得、削除などの機能を提供します。
 */
public class LocationController {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LocationController.class);

	@Autowired
	private LocationService locationService;
	@Autowired
	UserRepository userRepository;

	/**
	 * 新しい勤務地を登録するエンドポイントです。
	 *
	 * @param locationRequest 登録する勤務地情報
	 * @return 登録された勤務地情報またはエラーメッセージ
	 */
	@PostMapping
	public ResponseEntity<?> createLocation(@RequestBody LocationRequest locationRequest) {
		try {
			Location location = locationService.createLocation(locationRequest);
			return ResponseEntity.status(HttpStatus.CREATED).body(location);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("勤務地登録に失敗しました。");
		}
	}

	/**
	 * 指定されたIDの勤務地情報を取得するエンドポイントです。
	 *
	 * @param id 勤務地ID
	 * @return 該当する勤務地情報または404
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
		return locationService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * すべての勤務地情報を取得するエンドポイントです。
	 *
	 * @return 勤務地リスト
	 */
	@GetMapping
	public ResponseEntity<List<Location>> getAllLocations() {
		List<Location> locations = locationService.findAll();
		return ResponseEntity.ok(locations);
	}

	/**
	 * 指定されたIDの勤務地情報を削除するエンドポイントです。
	 *
	 * @param id 削除する勤務地ID
	 * @return 削除成功メッセージまたはエラーメッセージ
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteLocation(@PathVariable Long id, @RequestParam Long currentUserId) {
		User currentUser = userRepository.findById(currentUserId)
				.orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません。"));
		logger.info("DELETE /api/locations/{} called by user: {}", id,
				currentUser != null ? currentUser.getUsername() : "Unknown");
		try {
			locationService.deleteLocation(id, currentUser);
			logger.info("Location with ID {} deleted successfully by {}.", id,
					currentUser != null ? currentUser.getUsername() : "Unknown");
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (EntityNotFoundException e) {
			logger.warn("Location with ID {} not found: {}", id, e.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (AccessDeniedException e) {
			logger.warn("Access denied for deleting location ID {} by user {}: {}", id,
					currentUser != null ? currentUser.getUsername() : "Unknown", e.getMessage());
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (Exception e) {
			logger.error("Error deleting location with ID {}: {}", id, e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<Location> getLocationByUserId(@PathVariable Long userId) {
	    User user = userRepository.findById(userId)
	                          .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
	    Location location = user.getLocation();
	    if (location == null) {
	        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 勤務地が設定されていない場合
	    }
	    return new ResponseEntity<>(location, HttpStatus.OK);
	}
}
