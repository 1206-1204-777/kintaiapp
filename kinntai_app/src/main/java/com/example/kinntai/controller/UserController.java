package com.example.kinntai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.UserLocationResponse;
import com.example.kinntai.service.impl.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        try {
            return userService.findById(userId)
                    .map(user -> ResponseEntity.ok(user))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{userId}/location")
    public ResponseEntity<UserLocationResponse> getUserLocation(@PathVariable Long userId) {
        try {
            UserLocationResponse response = userService.getUserLocationInfo(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}