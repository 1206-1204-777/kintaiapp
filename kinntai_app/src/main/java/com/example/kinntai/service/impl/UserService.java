package com.example.kinntai.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.UserLocationResponse;
import com.example.kinntai.entity.Location;
import com.example.kinntai.entity.User;
import com.example.kinntai.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * ユーザー名でユーザーを検索します
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * IDでユーザーを検索します
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * すべてのユーザーを取得します
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * ユーザーに勤務地を割り当てます
     */
    @Transactional
    public User assignLocationToUser(User user, Location location) {
        user.setLocation(location);
        return userRepository.save(user);
    }

    /**
     * ユーザー情報を更新します
     */
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
    public UserLocationResponse getUserLocationInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        UserLocationResponse response = new UserLocationResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());

        if (user.getLocation() != null) {
            Location location = user.getLocation();
            response.setLocationId(location.getId());
            response.setLocationName(location.getName());
            response.setStartTime(location.getStartTime());
            response.setEndTime(location.getEndTime());
        }

        return response;
    }
    
    
}