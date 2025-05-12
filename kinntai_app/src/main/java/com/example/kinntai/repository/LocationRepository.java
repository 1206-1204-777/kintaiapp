package com.example.kinntai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    // カスタムクエリが必要な場合はここに追加
}