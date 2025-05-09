package com.example.kinntai.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kinntai.dto.LoginRequest;
import com.example.kinntai.dto.SignupRequest;
import com.example.kinntai.dto.UserResponse;
import com.example.kinntai.entity.User;
import com.example.kinntai.entity.WorkingHour;
import com.example.kinntai.repository.UserRepository;
import com.example.kinntai.repository.WorkingHourRepository;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * ユーザー登録
     */
    @Transactional
    public UserResponse registerUser(SignupRequest request) {
        // ユーザー名の重複チェック
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("ユーザー名はすでに使用されています");
        }
        
        // 新しいユーザーを作成
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 実際のアプリではハッシュ化すべき
        user.setStatus(true); // デフォルトでアクティブ
        
        // ユーザーIDを自動生成（最大値+1）
        Long nextUserId = generateNextUserId();
        user.setUserId(nextUserId);
        
        
        // 保存
        user = userRepository.save(user);
        
        // レスポンス返却
        return new UserResponse(user.getId(), user.getUserId(), user.getUsername(), user.isStatus());
    }
    
    /**
     * ログイン
     */
    public UserResponse login(LoginRequest request) {
        // ユーザー名でユーザーを検索
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("ユーザーが見つかりません");
        }
        
        User user = userOpt.get();
        
        // パスワードチェック（実際のアプリではハッシュ比較をすべき）
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("パスワードが間違っています");
        }
        
        // ステータスチェック
        if (!user.isStatus()) {
            throw new RuntimeException("このアカウントは無効化されています");
        }
        
        // レスポンス返却
        return new UserResponse(user.getId(), user.getUserId(), user.getUsername(), user.isStatus());
    }
    
    /**
     * 次のユーザーIDを生成
     */
    private Long generateNextUserId() {
        // 最大のユーザーIDを取得し、+1 する
        return userRepository.findMaxUserId()
                .map(maxId -> maxId + 1L)
                .orElse(1000L); // 初期値（例: 1000から開始）
    }
    
    @Autowired
    private WorkingHourRepository workingHourRepository;

    public void setDefaultWorkingHour(User user) {
        WorkingHour wh = new WorkingHour();
        wh.setUser(user);
        wh.setWorkStartTime(LocalTime.of(9, 0));
        wh.setWorkEndTime(LocalTime.of(18, 0));
        wh.setEffectiveFrom(LocalDate.now());
        wh.setCreatedAt(LocalDateTime.now());

        workingHourRepository.save(wh);
    }

}