package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import com.soprasteria.clinic.appointment.service.AppointmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentControllerTest {

    @InjectMocks
    private AppointmentController appointmentController;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private Authentication authentication;

    private AppointmentDTO appointmentDTO;
    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // common DTO
        appointmentDTO = new AppointmentDTO();
        appointmentDTO.setAppointment_id(10L);
        appointmentDTO.setAppointment_date(LocalDate.now());
        appointmentDTO.setAppointment_startTime(LocalTime.of(9, 0));
        appointmentDTO.setAppointment_endTime(LocalTime.of(10, 0));
        appointmentDTO.setAppointment_status("Booked");

        // Patient entity
        patient = new Patient();
        patient.setPatient_id(5L);
        patient.setUsername("patientUser");

        // Doctor entity
        doctor = new Doctor();
        doctor.setDoctor_id(7L);
        doctor.setUsername("doctorUser");
    }

    @Test
    void testBookAppointment_Success() {
        when(authentication.getName()).thenReturn("patientUser");
        when(appointmentService.bookAppointment(appointmentDTO, "patientUser"))
                .thenReturn(appointmentDTO);

        ResponseEntity<AppointmentDTO> resp = appointmentController.bookAppointment(
                appointmentDTO, "patientUser", authentication);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals(appointmentDTO.getAppointment_id(), resp.getBody().getAppointment_id());
    }

    @Test
    void testBookAppointment_WrongUser() {
        when(authentication.getName()).thenReturn("otherUser");

        ResponseEntity<AppointmentDTO> resp = appointmentController.bookAppointment(
                appointmentDTO, "patientUser", authentication);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(appointmentService, never()).bookAppointment(any(), any());
    }

    @Test
    void testViewAllAppointmentsForPatient_AsAdmin() {
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));

        // Mock pagination
        Page<AppointmentDTO> pagedAppointments = new PageImpl<>(List.of(appointmentDTO));
        when(appointmentService.viewAllAppointmentsForPatient(5L, 0, 10)).thenReturn(pagedAppointments);

        ResponseEntity<?> resp = appointmentController.viewAllAppointmentsForPatient(
                5L, 0, 10, authentication);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        Page<AppointmentDTO> result = (Page<AppointmentDTO>) resp.getBody();
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testViewAllAppointmentsForPatient_AsSelf() {
        when(authentication.getName()).thenReturn("patientUser");
        when(authentication.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );

        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));

        // Mock pagination
        Page<AppointmentDTO> pagedAppointments = new PageImpl<>(List.of(appointmentDTO));
        when(appointmentService.viewAllAppointmentsForPatient(5L, 0, 10)).thenReturn(pagedAppointments);

        ResponseEntity<?> resp = appointmentController.viewAllAppointmentsForPatient(
                5L, 0, 10, authentication);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void testViewAllAppointmentsForPatient_Forbidden() {
        when(authentication.getName()).thenReturn("anotherUser");
        when(authentication.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );

        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));

        ResponseEntity<?> resp = appointmentController.viewAllAppointmentsForPatient(
                5L, 0, 10, authentication);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertTrue(resp.getBody().toString().contains("Access denied"));
        verify(appointmentService, never()).viewAllAppointmentsForPatient(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testViewAllAppointmentsForPatient_NotFound() {
        when(patientRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = appointmentController.viewAllAppointmentsForPatient(
                5L, 0, 10, authentication);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("Patient not found.", resp.getBody());
    }

    @Test
    void testViewAllAppointmentsForDoctor_AsDoctor() {
        when(authentication.getName()).thenReturn("doctorUser");
        when(authentication.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));

        // Mock pagination
        Page<AppointmentDTO> pagedAppointments = new PageImpl<>(List.of(appointmentDTO));
        when(appointmentService.viewAllAppointmentsForDoctor(7L, 0, 10)).thenReturn(pagedAppointments);

        ResponseEntity<?> resp = appointmentController.viewAllAppointmentsForDoctor(
                7L, 0, 10, authentication);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void testViewAllAppointmentsForDoctor_Forbidden() {
        when(authentication.getName()).thenReturn("otherDoc");
        when(authentication.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
        );

        when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));

        ResponseEntity<?> resp = appointmentController.viewAllAppointmentsForDoctor(
                7L, 0, 10, authentication);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void testViewAllAppointments() {
        // Mock pagination
        Page<AppointmentDTO> pagedAppointments = new PageImpl<>(List.of(appointmentDTO));
        when(appointmentService.viewAllAppointments(0, 10)).thenReturn(pagedAppointments);

        ResponseEntity<Page<AppointmentDTO>> resp = appointmentController.viewAllAppointments(0, 10);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().getTotalElements());
    }

    @Test
    void testCancelAppointment_Success() {
        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));
        when(authentication.getName()).thenReturn("patientUser");
        when(appointmentService.cancelAppointment(10L, 5L))
                .thenReturn("Cancelled");

        ResponseEntity<String> resp = appointmentController.cancelAppointment(
                10L, 5L, authentication);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Cancelled", resp.getBody());
    }

    @Test
    void testCancelAppointment_Forbidden() {
        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));
        when(authentication.getName()).thenReturn("otherUser");

        ResponseEntity<String> resp = appointmentController.cancelAppointment(
                10L, 5L, authentication);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertEquals("Forbidden", resp.getBody());
        verify(appointmentService, never()).cancelAppointment(anyLong(), anyLong());
    }

    @Test
    void testCancelAppointment_NotFoundPatient() {
        when(patientRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseEntity<String> resp = appointmentController.cancelAppointment(
                10L, 5L, authentication);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("Patient not found", resp.getBody());
    }
}
