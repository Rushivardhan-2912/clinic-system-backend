package com.soprasteria.clinic.appointment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.PatientNotFoundException;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.PatientRepository;

import com.soprasteria.clinic.appointment.util.NullPropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    private static final Logger logger = LogManager.getLogger(PatientServiceImpl.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private GlobalMapper globalMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Optional<Patient> findByUsername(String username) {
        logger.info("Attempting to find patient with username: {}", username);
        return patientRepository.findByUsername(username);
    }

    @Override
    public Patient registerPatient(Patient patient) {
        logger.info("Registering new patient with username: {}", patient.getUsername());
        return patientRepository.save(patient);
    }

    @Override
    public List<PatientDTO> getAllPatients(int page, int size) {
        logger.info("Retrieving all patients");
        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> patientPage = patientRepository.findAll(pageable);
        logger.debug("Found {} patients", patientPage.getTotalElements());
        return patientPage.map(globalMapper::toPatientDTO).getContent(); // only content
    }

    @Override
    public PatientDTO updatePatient(PatientDTO updatedPatientDTO, Long patientId, String loggedInUsername) {
        Patient existingPatient = patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    logger.error("Patient not found with ID: {}", patientId);
                    return new PatientNotFoundException("Patient not found with ID: " + patientId);
                });

        // Ensure only the owner (logged-in patient) can update their own data
        if (!existingPatient.getUsername().equals(loggedInUsername)) {
            logger.warn("User {} attempted to update data for {}", loggedInUsername, existingPatient.getUsername());
            throw new ClinicExceptionHandler.UnauthorizedAccessException("You are not authorized to update data for another patient.");
        }

        logger.info("Updating patient data for ID: {}", patientId);

        // Copy non-null properties from DTO to existing entity
        BeanUtils.copyProperties(updatedPatientDTO, existingPatient, NullPropertyUtils.getNullPropertyNames(updatedPatientDTO));

        Patient savedPatient = patientRepository.save(existingPatient);
        logger.info("Patient data updated successfully for username: {}", existingPatient.getUsername());

        return globalMapper.toPatientDTO(savedPatient);
    }

    @Override
    public String deletePatientById(Long id, String loggedInUsername) {
        logger.info("Attempting to delete patient with ID: {}", id);

        Optional<Patient> optionalPatient = patientRepository.findById(id);
        if (!optionalPatient.isPresent()) {
            logger.error("Patient not found with ID: {}", id);
            throw new PatientNotFoundException("Patient with ID " + id + " not found");
        }

        Patient patient = optionalPatient.get();

        // Check if logged-in user is authorized
        if (!patient.getUsername().equals(loggedInUsername)) {
            logger.warn("Unauthorized delete attempt by user: {}", loggedInUsername);
            throw new ClinicExceptionHandler.UnauthorizedAccessException("You are not authorized to delete this patient");
        }

        patientRepository.deleteById(id);
        logger.info("Successfully deleted patient with ID: {}", id);
        return "Patient with ID " + id + " deleted successfully.";
    }

}
