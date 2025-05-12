package com.example.kinntai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.dto.LocationRequest;
import com.example.kinntai.entity.Location;
import com.example.kinntai.service.impl.LocationService;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody LocationRequest locationRequest) {
        try {
            Location location = locationService.createLocation(locationRequest);
            return ResponseEntity.ok(location);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        return locationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<?> getAllLocations() {
        return ResponseEntity.ok(locationService.findAll());
    }
}