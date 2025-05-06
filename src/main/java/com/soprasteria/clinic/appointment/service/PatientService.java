package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Patient;

import java.util.List;
import java.util.Optional;

public interface PatientService {
    Optional<Patient> findByUsername(String username);
    Patient registerPatient(Patient patient);
    List<PatientDTO> getAllPatients(int page, int size);
    PatientDTO updatePatient(PatientDTO updatedPatientDTO, Long id, String loggedInUsername);
    String deletePatientById(Long id, String loggedInUsername);
}
