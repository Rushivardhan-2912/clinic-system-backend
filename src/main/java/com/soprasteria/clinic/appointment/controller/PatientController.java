package com.soprasteria.clinic.appointment.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.service.PatientService;

import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

	private static final Logger logger = LogManager.getLogger(PatientController.class);

	@Autowired
	private PatientService patientService;

	@PostMapping
	public ResponseEntity<Patient> registerPatient(@RequestBody Patient patient) {
		return new ResponseEntity<>(patientService.registerPatient(patient), HttpStatus.CREATED);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<PatientDTO>> getAllPatients(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			Authentication authentication) {
		return new ResponseEntity<>(patientService.getAllPatients(page, size), HttpStatus.OK);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('PATIENT')")
	public ResponseEntity<PatientDTO> updatePatient(@RequestBody PatientDTO patientDTO,
													@PathVariable Long id,
													Authentication authentication) {
		PatientDTO updated = patientService.updatePatient(patientDTO, id, authentication.getName());
		HttpStatus status = updated.getPatient_name().equals("Unauthorized") ? HttpStatus.FORBIDDEN : HttpStatus.OK;
		return new ResponseEntity<>(updated, status);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> deletePatientById(@PathVariable Long id, Authentication authentication) {
		return new ResponseEntity<>(patientService.deletePatientById(id,authentication.getName()), HttpStatus.OK);
	}
}

