package com.example.kinntai.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    public Location(String name, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}