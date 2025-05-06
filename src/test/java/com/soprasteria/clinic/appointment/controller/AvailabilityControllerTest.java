package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import com.soprasteria.clinic.appointment.service.AvailabilityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvailabilityControllerTest {

    @InjectMocks
    private AvailabilityController availabilityController;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private Authentication authentication;

    private AvailabilityDTO availabilityDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        availabilityDTO = new AvailabilityDTO();
        availabilityDTO.setAvailability_id(1L);
        availabilityDTO.setAvailability_date(LocalDate.now());
        availabilityDTO.setAvailability_startTime(LocalTime.of(9, 0));
        availabilityDTO.setAvailability_endTime(LocalTime.of(11, 0));
        availabilityDTO.setAvailability_status("Available");
    }

    @Test
    void testAddAvailability_Success() {
        when(authentication.getName()).thenReturn("doctor123");
        when(availabilityService.addAvailability(any(AvailabilityDTO.class), eq("doctor123")))
                .thenReturn(availabilityDTO);

        ResponseEntity<AvailabilityDTO> response = availabilityController.addAvailability(
                availabilityDTO, "doctor123", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(availabilityDTO.getAvailability_id(), response.getBody().getAvailability_id());
        verify(availabilityService, times(1)).addAvailability(any(AvailabilityDTO.class), eq("doctor123"));
    }

    @Test
    void testAddAvailability_Forbidden() {
        when(authentication.getName()).thenReturn("wrongDoctor");

        ResponseEntity<AvailabilityDTO> response = availabilityController.addAvailability(
                availabilityDTO, "doctor123", authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(availabilityService, never()).addAvailability(any(), any());
    }

    @Test
    void testGetAllAvailabilities_AdminAccess() {
        // Mock authentication and authorities
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getAuthorities()).thenReturn(
                (Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Mocking service method for paginated availabilities
        when(availabilityService.getAllAvailabilities(eq(0), eq(10))).thenReturn(List.of(availabilityDTO));

        // Calling the controller method
        ResponseEntity<?> response = availabilityController.getAllAvailabilities(0, 10, authentication);

        // Asserting the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        verify(availabilityService, times(1)).getAllAvailabilities(0, 10);
    }

    @Test
    void testGetAllAvailabilities_PatientAccess() {
        // Mock authentication and authorities
        when(authentication.getName()).thenReturn("patient");
        when(authentication.getAuthorities()).thenReturn(
                (Collection) List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));

        // Mocking service method for paginated availabilities
        when(availabilityService.getAllAvailabilities(eq(0), eq(10))).thenReturn(List.of(availabilityDTO));

        // Calling the controller method
        ResponseEntity<?> response = availabilityController.getAllAvailabilities(0, 10, authentication);

        // Asserting the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(availabilityService, times(1)).getAllAvailabilities(0, 10);
    }

    @Test
    void testGetAllAvailabilities_Forbidden() {
        // Mock authentication and authorities
        when(authentication.getName()).thenReturn("doctor");
        when(authentication.getAuthorities()).thenReturn(
                (Collection) List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")));

        // Calling the controller method
        ResponseEntity<?> response = availabilityController.getAllAvailabilities(0, 10, authentication);

        // Asserting the response
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied: You do not have permission to view availabilities.", response.getBody());
        verify(availabilityService, never()).getAllAvailabilities(eq(0), eq(10));
    }

    @Test
    void testUpdateAvailability_Success() {
        when(authentication.getName()).thenReturn("doctor123");
        when(availabilityService.updateAvailability(any(AvailabilityDTO.class), eq("doctor123")))
                .thenReturn(availabilityDTO);

        ResponseEntity<AvailabilityDTO> response = availabilityController.updateAvailability(
                availabilityDTO, "doctor123", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(availabilityDTO.getAvailability_id(), response.getBody().getAvailability_id());
        verify(availabilityService, times(1)).updateAvailability(any(AvailabilityDTO.class), eq("doctor123"));
    }

    @Test
    void testUpdateAvailability_WrongUser() {
        when(authentication.getName()).thenReturn("wrongDoctor");

        ResponseEntity<AvailabilityDTO> response = availabilityController.updateAvailability(
                availabilityDTO, "doctor123", authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(availabilityService, never()).updateAvailability(any(), any());
    }

    @Test
    void testDeleteAvailability() {
        when(authentication.getName()).thenReturn("doctor123");

        ResponseEntity<String> response = availabilityController.deleteAvailability(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Availability deleted successfully", response.getBody());
        verify(availabilityService, times(1)).deleteAvailability(1L, "doctor123");
    }
}
