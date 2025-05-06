package com.soprasteria.clinic.appointment.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.service.PatientService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // MockitoExtension will initialize mocks and inject them
        mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();
    }

    @Test
    void testRegisterPatient() {
        // Arrange
        Patient input = new Patient();
        input.setUsername("john123");
        when(patientService.registerPatient(any(Patient.class))).thenReturn(input);

        // Act
        ResponseEntity<Patient> response = patientController.registerPatient(input);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("john123", response.getBody().getUsername());
    }

    @Test
    void testGetAllPatientsPaginated() {
        // Arrange
        PatientDTO p1 = new PatientDTO();
        p1.setPatient_name("John Doe");
        PatientDTO p2 = new PatientDTO();
        p2.setPatient_name("Jane Doe");

        List<PatientDTO> patientDTOList = Arrays.asList(p1, p2);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<PatientDTO> page = new PageImpl<>(patientDTOList, pageRequest, 2);

        when(patientService.getAllPatientsPaginated(0, 10)).thenReturn(page);

        // Act
        ResponseEntity<Page<PatientDTO>> response =
                patientController.getAllPatients(0, 10, mock(Authentication.class));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("John Doe", response.getBody().getContent().get(0).getPatient_name());
        assertEquals("Jane Doe", response.getBody().getContent().get(1).getPatient_name());
    }

    @Test
    void testUpdatePatient_Authorized() {
        // Arrange
        PatientDTO dto = new PatientDTO();
        dto.setPatient_name("Updated Name");
        when(patientService.updatePatient(eq(dto), eq("john123"))).thenReturn(dto);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("john123");

        // Act
        ResponseEntity<PatientDTO> response =
                patientController.updatePatient(dto, "john123", auth);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Name", response.getBody().getPatient_name());
    }

    @Test
    void testUpdatePatient_Unauthorized() {
        // Arrange
        PatientDTO dto = new PatientDTO();
        dto.setPatient_name("Should Not Apply");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("anotherUser");

        // Act
        ResponseEntity<PatientDTO> response =
                patientController.updatePatient(dto, "john123", auth);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().getPatient_name());
        assertEquals("Unauthorized", response.getBody().getPatient_email());
        assertEquals("Unauthorized", response.getBody().getPatient_phoneNumber());
    }

    @Test
    void testDeletePatientById() {
        // Arrange
        Long id = 5L;
        doNothing().when(patientService).deletePatientById(id);

        // Act
        ResponseEntity<String> response =
                patientController.deletePatientById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Patient with ID 5 deleted successfully"));
    }
}
