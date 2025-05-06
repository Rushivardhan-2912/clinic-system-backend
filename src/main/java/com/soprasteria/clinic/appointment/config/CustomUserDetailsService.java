package com.soprasteria.clinic.appointment.config;

import com.soprasteria.clinic.appointment.entity.Admin;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.repo.AdminRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LogManager.getLogger(CustomUserDetailsService.class);

    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Check Admin
        Admin admin = adminRepo.findByUsername(username);
        if (admin != null) {
            logger.info("Found admin with username: {}", username);
            return new User(
                    admin.getUsername(),
                    admin.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        // Check Doctor
        Optional<Doctor> doctor = doctorRepo.findByUsername(username);
        if (doctor.isPresent()) {
            logger.info("Found doctor with username: {}", username);
            return new User(
                    doctor.get().getUsername(),
                    doctor.get().getDoctor_password(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_DOCTOR"))
            );
        }

        // Check Patient
        Optional<Patient> patient = patientRepo.findByUsername(username);
        if (patient.isPresent()) {
            logger.info("Found patient with username: {}", username);
            return new User(
                    patient.get().getUsername(),
                    patient.get().getPatient_password(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT"))
            );
        }
        // If no match found
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
