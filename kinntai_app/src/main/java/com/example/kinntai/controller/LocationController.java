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
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.LocationRequest;
import com.example.kinntai.entity.Location;
import com.example.kinntai.entity.User;
import com.example.kinntai.service.LocationService;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LocationController.class);
	@Autowired
	private LocationService locationService;

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

	@GetMapping("/{id}")
	public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
		return locationService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping
	public ResponseEntity<List<Location>> getAllLocations() {
		return ResponseEntity.ok(locationService.findAll());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteLocation(@PathVariable Long id, User currentUser) {
		logger.info("DELETE /api/locations/{} called by user: {}", id,
				currentUser != null ? currentUser.getUsername() : "Unknown");
		try {
			locationService.deleteLocation(id, currentUser);
			logger.info("Location with ID {} deleted successfully by {}.", id,
					currentUser != null ? currentUser.getUsername() : "Unknown");
			return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content を返す
		} catch (EntityNotFoundException e) {
			logger.warn("Location with ID {} not found: {}", id, e.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found を返す
		} catch (AccessDeniedException e) {
			logger.warn("Access denied for deleting location ID {} by user {}: {}", id,
					currentUser != null ? currentUser.getUsername() : "Unknown", e.getMessage());
			return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden を返す
		} catch (Exception e) {
			logger.error("Error deleting location with ID {}: {}", id, e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error を返す
		}
	}
}