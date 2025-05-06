package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AvailabilityService {
    AvailabilityDTO addAvailability(AvailabilityDTO availabilityDTO, Long id, Authentication authentication);
    AvailabilityDTO updateAvailability(AvailabilityDTO availabilityDTO, Long id, Authentication authentication);
    List<AvailabilityDTO> getAllAvailabilities(int page, int size);
    String deleteAvailability(Long id,String username, Authentication authentication);
}

