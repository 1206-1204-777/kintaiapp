package com.example.kinntai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.Location;
import com.example.kinntai.entity.UserRole;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
	@Query("SELECT DISTINCT l FROM Location l JOIN l.users u WHERE u.role = :role")
	List<Location> findByUsersRole(@Param("role") UserRole role);


}