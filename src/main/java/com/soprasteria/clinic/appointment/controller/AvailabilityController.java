package com.soprasteria.clinic.appointment.controller;

import java.util.List;
import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.soprasteria.clinic.appointment.service.AvailabilityService;

@RestController
@RequestMapping("/api/v1/availabilities")
public class AvailabilityController {
    private static final Logger logger = LogManager.getLogger(AvailabilityController.class);

    @Autowired
    private AvailabilityService availabilityService;

    @PostMapping("/doctor/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AvailabilityDTO> addAvailability(
            @RequestBody AvailabilityDTO availabilityDTO,
            @PathVariable Long id,
            Authentication authentication) {
        AvailabilityDTO result = availabilityService.addAvailability(availabilityDTO, id, authentication);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<List<AvailabilityDTO>> getAllAvailabilities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        List<AvailabilityDTO> availabilities = availabilityService.getAllAvailabilities(page, size);
        return ResponseEntity.ok(availabilities);
    }

    @PutMapping("/doctor/availability/{availabilityId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AvailabilityDTO> updateAvailability(
            @RequestBody AvailabilityDTO availabilityDTO,
            @PathVariable Long availabilityId,
            Authentication authentication) {

        AvailabilityDTO updated = availabilityService.updateAvailability(availabilityDTO, availabilityId, authentication);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/doctor/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> deleteAvailability(
            @PathVariable Long id,
            @PathVariable String username,
            Authentication authentication) {
        String message = availabilityService.deleteAvailability(id, username,authentication);
        return ResponseEntity.ok(message);
    }
}
