package com.soprasteria.clinic.appointment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import com.soprasteria.clinic.appointment.entity.Availability;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.AvailabilityNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.DoctorNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.UnauthorizedAccessException;

import com.soprasteria.clinic.appointment.util.NullPropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final Logger logger = LogManager.getLogger(AvailabilityServiceImpl.class);

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private GlobalMapper globalMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    public AvailabilityDTO addAvailability(AvailabilityDTO availabilityDTO, Long doctorId, Authentication authentication) {
        String loggedInUsername = authentication.getName();
        logger.info("Adding availability for doctor with ID: {}", doctorId);

        // Fetch doctor by ID instead of username
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> {
                    logger.error("Doctor not found with ID: {}", doctorId);
                    return new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
                });

        // Ensure the logged-in doctor is the one making the request
        if (!doctor.getUsername().equals(loggedInUsername)) {
            logger.warn("Doctor with ID {} is not authorized to add availability.", doctorId);
            throw new UnauthorizedAccessException("You are not authorized to add availability for this doctor.");
        }

//        Validate time logic if both times are provided
        if (availabilityDTO.getAvailability_startTime() != null && availabilityDTO.getAvailability_endTime() != null &&
                availabilityDTO.getAvailability_startTime().isAfter(availabilityDTO.getAvailability_endTime())) {
            logger.warn("Start time {} is after end time {}", availabilityDTO.getAvailability_startTime(), availabilityDTO.getAvailability_endTime());
            throw new IllegalArgumentException("Start time cannot be after end time.");
        }

        // Map the DTO to the entity and set default availability status
        Availability availability = globalMapper.toAvailabilityEntity(availabilityDTO, doctor);
        availability.setAvailability_status("Available");

        // Save the availability entity
        Availability saved = availabilityRepository.save(availability);

        logger.debug("Availability saved successfully with ID: {}", saved.getAvailability_id());
        return globalMapper.toAvailabilityDTO(saved);
    }

    @Override
    public List<AvailabilityDTO> getAllAvailabilities(int page, int size) {
        logger.info("Retrieving availabilities with pagination: page {}, size {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Availability> availabilityPage = availabilityRepository.findAll(pageable);

        logger.debug("Found {} availabilities", availabilityPage.getTotalElements());
        return availabilityPage.map(globalMapper::toAvailabilityDTO).getContent();
    }

    @Override
    public AvailabilityDTO updateAvailability(AvailabilityDTO availabilityDTO, Long availabilityId, Authentication authentication) {
        String loggedInUsername = authentication.getName();

        // Fetch availability using ID from path
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new AvailabilityNotFoundException("Availability not found with ID: " + availabilityId));

        // Ensure the doctor associated with this availability is the logged-in user
        Doctor doctor = availability.getDoctor();
        if (!doctor.getUsername().equals(loggedInUsername)) {
            throw new UnauthorizedAccessException("You are not authorized to update this availability.");
        }

        // Copy non-null fields from DTO to entity
        BeanUtils.copyProperties(availabilityDTO, availability, NullPropertyUtils.getNullPropertyNames(availabilityDTO));

//        Validate time logic if both times are provided
        if (availabilityDTO.getAvailability_startTime() != null && availabilityDTO.getAvailability_endTime() != null &&
                availabilityDTO.getAvailability_startTime().isAfter(availabilityDTO.getAvailability_endTime())) {
            logger.warn("Start time {} is after end time {}", availabilityDTO.getAvailability_startTime(), availabilityDTO.getAvailability_endTime());
            throw new IllegalArgumentException("Start time cannot be after end time.");
        }

        // Ensure doctor and ID are not overwritten unintentionally
        availability.setDoctor(doctor); // Reinforce doctor assignment
        availability.setAvailability_id(availabilityId); // Reinforce correct ID
        availability.setAvailability_status("Available");

        Availability updated = availabilityRepository.save(availability);
        logger.debug("Availability updated successfully with ID: {}", updated.getAvailability_id());

        return globalMapper.toAvailabilityDTO(updated);
    }


    @Override
    public String deleteAvailability(Long id, String username, Authentication authentication) {
        validateLoggedInUser(username, authentication);

        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new AvailabilityNotFoundException("Availability not found with ID: " + id));

        Doctor doctor = doctorRepository.findByUsername(username)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with username: " + username));

        if (!availability.getDoctor().getDoctor_id().equals(doctor.getDoctor_id())) {
            throw new UnauthorizedAccessException("You are not authorized to delete this availability");
        }

        availabilityRepository.deleteById(id);
        logger.info("Availability deleted successfully with ID: {}", id);
        return "Availability deleted successfully with ID: " + id;
    }

    private void validateLoggedInUser(String username, Authentication authentication) {
        if (authentication == null || !authentication.getName().equals(username)) {
            throw new UnauthorizedAccessException("You are not authorized to perform this operation.");
        }
    }
}
