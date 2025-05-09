package com.example.kinntai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kinntai.entity.MonthlySummary;

@Repository
public interface MonthRepository extends JpaRepository<MonthlySummary,Integer>{

}
