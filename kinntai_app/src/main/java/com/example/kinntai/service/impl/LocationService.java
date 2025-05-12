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
import com.example.kinntai.repository.LocationRepository;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 新しい勤務地を作成します
     */
    @Transactional
    public Location createLocation(LocationRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("勤務地名は必須です");
        }
        
        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty()) {
            throw new IllegalArgumentException("開始時刻は必須です");
        }
        
        if (request.getEndTime() == null || request.getEndTime().trim().isEmpty()) {
            throw new IllegalArgumentException("終了時刻は必須です");
        }
        
        LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
        LocalTime endTime = LocalTime.parse(request.getEndTime(), TIME_FORMATTER);
        
        Location location = new Location(request.getName(), startTime, endTime);
        return locationRepository.save(location);
    }
    
    /**
     * LocationエンティティからLocationを作成
     */
    @Transactional
    public Location createLocation(Location location) {
        if (location.getName() == null || location.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("勤務地名は必須です");
        }
        
        if (location.getStartTime() == null) {
            throw new IllegalArgumentException("開始時刻は必須です");
        }
        
        if (location.getEndTime() == null) {
            throw new IllegalArgumentException("終了時刻は必須です");
        }
        
        return locationRepository.save(location);
    }

    /**
     * IDで勤務地を検索します
     */
    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }
    
    /**
     * すべての勤務地を取得します
     */
    public List<Location> findAll() {
        return locationRepository.findAll();
    }
    
    /**
     * 勤務地を更新します
     */
    @Transactional
    public Location updateLocation(Long id, LocationRequest request) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("勤務地が見つかりません: " + id));
        
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
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
    @Transactional
    public boolean deleteLocation(Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}