package com.soprasteria.clinic.appointment.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soprasteria.clinic.appointment.entity.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

	Optional<Patient> findByUsername(String username);

}
