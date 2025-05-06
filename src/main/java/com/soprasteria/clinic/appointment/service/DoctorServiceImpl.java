package com.soprasteria.clinic.appointment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import org.springframework.security.core.Authentication;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.DoctorNotFoundException;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.util.NullPropertyUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;


import java.util.List;
import java.util.Optional;

@Service
public class DoctorServiceImpl implements DoctorService {

    private static final Logger logger = LogManager.getLogger(DoctorServiceImpl.class);

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GlobalMapper globalMapper;

    @Override
    public Optional<Doctor> findByUsername(String username) {
        logger.info("Loading Doctor with username: {}", username);
        return doctorRepository.findByUsername(username);
    }

    @Override
    public Doctor registerDoctor(Doctor doctor) {
        logger.info("Registering new doctor with username: {}", doctor.getUsername());
        return doctorRepository.save(doctor);
    }

    @Override
    public List<DoctorDTO> getAllDoctors(int page, int size, Authentication authentication) {
        String loginUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isPatient = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PATIENT"));

        if (!(isAdmin || isPatient)) {
            logger.warn("Access denied for user: {}", loginUsername);
            throw new SecurityException("Access denied: You are not authorized to view the doctor list.");
        }

        logger.info("Fetching all doctors with pagination for user: {}, page: {}, size: {}", loginUsername, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Doctor> doctorsPage = doctorRepository.findAll(pageable);
        return doctorsPage.map(globalMapper::toDoctorDTO).getContent();
    }

    @Override
    public DoctorDTO updateDoctor(DoctorDTO updatedDoctorDTO, Long doctorId, Authentication authentication) {
        // Get the logged-in user's username
        String loginUsername = authentication.getName();

        // Fetch the existing doctor by ID
        Doctor existingDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> {
                    logger.error("Doctor not found with ID: {}", doctorId);
                    return new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
                });

        // Ensure only the logged-in doctor can update their own data
        if (!existingDoctor.getUsername().equals(loginUsername)) {
            logger.warn("User {} is trying to update details for another doctor with ID: {}", loginUsername, doctorId);
            throw new SecurityException("You are not authorized to update details for another doctor.");
        }

        logger.info("Updating doctor details for ID: {}", doctorId);

        // Copy only non-null properties from DTO to existing doctor entity
        BeanUtils.copyProperties(updatedDoctorDTO, existingDoctor, NullPropertyUtils.getNullPropertyNames(updatedDoctorDTO));

        // Save the updated doctor entity back to the database
        Doctor savedDoctor = doctorRepository.save(existingDoctor);
        logger.info("Doctor details updated successfully for ID: {}", doctorId);

        // Return the updated doctor as DTO
        return globalMapper.toDoctorDTO(savedDoctor);
    }

    @Override
    public String deleteDoctorById(Long id) {
        if (!doctorRepository.existsById(id)) {
            logger.error("Doctor with ID {} not found for deletion", id);
            throw new DoctorNotFoundException("Doctor with ID " + id + " not found");
        }

        doctorRepository.deleteById(id);
        logger.info("Successfully deleted doctor with ID: {}", id);
        return "Doctor with ID " + id + " deleted successfully.";
    }
}
