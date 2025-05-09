package com.example.kinntai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ユーザー名でユーザーを検索
    Optional<User> findByUsername(String username);
    
    // ユーザーIDでユーザーを検索
    Optional<User> findByUserId(Long userId);
    
    // ユーザー名の重複チェック
    boolean existsByUsername(String username);
    
    // 最大のユーザーIDを取得
    @Query("SELECT MAX(u.userId) FROM User u")
    Optional<Long> findMaxUserId();
}