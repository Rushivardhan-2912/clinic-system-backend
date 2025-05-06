package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface DoctorService {
    Optional<Doctor> findByUsername(String username);
    Doctor registerDoctor(Doctor doctor);
    List<DoctorDTO> getAllDoctors(int page, int size, Authentication authentication);
    DoctorDTO updateDoctor(DoctorDTO doctorDTO, Long id, Authentication authentication);
    String deleteDoctorById(Long id);
}
