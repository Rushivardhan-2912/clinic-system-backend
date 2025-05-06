package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import com.soprasteria.clinic.appointment.service.AppointmentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

	private static final Logger logger = LogManager.getLogger(AppointmentController.class);

	@Autowired
	AppointmentService appointmentService;

	@Autowired
	PatientRepository patientRepository;

	@PostMapping("/patient/{id}")
	@PreAuthorize("hasRole('PATIENT')")
	public ResponseEntity<AppointmentDTO> bookAppointment(@RequestBody AppointmentDTO appointmentDTO,
														  @PathVariable Long id, // Use patient ID
														  Authentication authentication) {
		AppointmentDTO bookedAppointment = appointmentService.bookAppointment(appointmentDTO, id, authentication);
		return new ResponseEntity<>(bookedAppointment, HttpStatus.CREATED);
	}

	@GetMapping("/patient/{id}")
	@PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
	public ResponseEntity<List<AppointmentDTO>> viewAppointmentsForPatient(@PathVariable Long id,
																		   @RequestParam(defaultValue = "0") int page,
																		   @RequestParam(defaultValue = "10") int size,
																		   Authentication authentication) {
		List<AppointmentDTO> appointments = appointmentService.viewAllAppointmentsForPatient(id, page, size, authentication);
		return ResponseEntity.ok(appointments);
	}

	@GetMapping("/doctor/{id}")
	@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
	public ResponseEntity<List<AppointmentDTO>> viewAppointmentsForDoctor(@PathVariable Long id,
																		  @RequestParam(defaultValue = "0") int page,
																		  @RequestParam(defaultValue = "10") int size,
																		  Authentication authentication) {
		List<AppointmentDTO> appointments = appointmentService.viewAllAppointmentsForDoctor(id, page, size, authentication);
		return ResponseEntity.ok(appointments);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<AppointmentDTO>> viewAllAppointments(@RequestParam(defaultValue = "0") int page,
																	@RequestParam(defaultValue = "10") int size) {
		List<AppointmentDTO> appointments = appointmentService.viewAllAppointments(page, size);
		return ResponseEntity.ok(appointments);
	}

	@PutMapping("/{appointmentId}")
	@PreAuthorize("hasRole('PATIENT')")
	public ResponseEntity<AppointmentDTO> updateAppointment(@RequestBody AppointmentDTO appointmentDTO,@PathVariable Long appointmentId,Authentication authentication) {
		AppointmentDTO updated = appointmentService.rescheduleAppointment(appointmentDTO, appointmentId, authentication.getName());
		return new ResponseEntity<>(updated, HttpStatus.OK);
	}

	@DeleteMapping("/{appointmentId}/patient/{patientId}")
	@PreAuthorize("hasRole('PATIENT')")
	public ResponseEntity<String> cancelAppointment(@PathVariable Long appointmentId,
													@PathVariable Long patientId,
													Authentication authentication) {
		String result = appointmentService.cancelAppointment(appointmentId, patientId, authentication);
		return ResponseEntity.ok(result);
	}
}
