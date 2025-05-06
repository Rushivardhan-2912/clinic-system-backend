package com.soprasteria.clinic.appointment.controller;

import java.util.List;

import com.soprasteria.clinic.appointment.entity.Doctor;
import org.springframework.security.core.Authentication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.service.DoctorService;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

	private static final Logger logger = LogManager.getLogger(DoctorController.class);

	@Autowired
	private DoctorService doctorService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Doctor> registerDoctor(@RequestBody Doctor doctor) {
		return new ResponseEntity<>(doctorService.registerDoctor(doctor), HttpStatus.CREATED);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
	public ResponseEntity<List<DoctorDTO>> getAllDoctors(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			Authentication authentication) {
		List<DoctorDTO> doctors = doctorService.getAllDoctors(page, size, authentication);
		return new ResponseEntity<>(doctors, HttpStatus.OK);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('DOCTOR')")
	public ResponseEntity<DoctorDTO> updateDoctor(@RequestBody DoctorDTO doctorDTO,
												  @PathVariable Long id,
												  Authentication authentication) {
		return new ResponseEntity<>(doctorService.updateDoctor(doctorDTO, id, authentication), HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> deleteDoctorById(@PathVariable Long id) {
		return new ResponseEntity<>(doctorService.deleteDoctorById(id), HttpStatus.OK);
	}
}