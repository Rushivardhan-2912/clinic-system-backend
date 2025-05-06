package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterDoctor() {
        DoctorDTO doctorDTO = new DoctorDTO();
        doctorDTO.setUsername("doc1");

        Doctor savedDoctor = new Doctor();
        savedDoctor.setUsername("doc1");

        when(doctorService.registerDoctor(any())).thenReturn(savedDoctor);

        ResponseEntity<Doctor> response = doctorController.registerDoctor(savedDoctor);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedDoctor.getUsername(), response.getBody().getUsername());
    }

    @Test
    void testGetAllDoctors_AuthorizedAdmin() {
        DoctorDTO doctor1 = new DoctorDTO();
        doctor1.setUsername("doc123");

        DoctorDTO doctor2 = new DoctorDTO();
        doctor2.setUsername("doc456");

        List<DoctorDTO> doctorList = Arrays.asList(doctor1, doctor2);
        Page<DoctorDTO> doctorsPage = new PageImpl<>(doctorList, PageRequest.of(0, 10), doctorList.size());

        when(doctorService.getAllDoctorsPaginated(0, 10)).thenReturn(doctorsPage);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        when(auth.getAuthorities()).thenAnswer(invocation -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        ResponseEntity<?> response = doctorController.getAllDoctors(0, 10, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(((Page<?>) response.getBody()).getContent().size() == 2);
    }

    @Test
    void testGetAllDoctors_AsPatient() {
        DoctorDTO doctor1 = new DoctorDTO();
        doctor1.setUsername("doc123");

        DoctorDTO doctor2 = new DoctorDTO();
        doctor2.setUsername("doc456");

        List<DoctorDTO> doctorList = Arrays.asList(doctor1, doctor2);
        Page<DoctorDTO> doctorsPage = new PageImpl<>(doctorList, PageRequest.of(0, 10), doctorList.size());

        when(doctorService.getAllDoctorsPaginated(0, 10)).thenReturn(doctorsPage);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("patient123");

        when(auth.getAuthorities()).thenAnswer(invocation -> List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));

        ResponseEntity<?> response = doctorController.getAllDoctors(0, 10, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(((Page<?>) response.getBody()).getContent().size() == 2);
    }

    @Test
    void testGetAllDoctors_Unauthorized() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("guest123");

        when(auth.getAuthorities()).thenAnswer(invocation -> List.of(new SimpleGrantedAuthority("ROLE_GUEST")));

        ResponseEntity<?> response = doctorController.getAllDoctors(0, 10, auth);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied: You are not authorized to view the doctor list.", response.getBody());
    }

    @Test
    void testUpdateDoctor_Success() {
        DoctorDTO inputDTO = new DoctorDTO();
        inputDTO.setDoctor_name("Updated Name");

        DoctorDTO updatedDTO = new DoctorDTO();
        updatedDTO.setDoctor_name("Updated Name");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("doc123");

        when(doctorService.updateDoctor(any(DoctorDTO.class), eq("doc123"))).thenReturn(updatedDTO);

        ResponseEntity<DoctorDTO> response = doctorController.updateDoctor(inputDTO, "doc123", auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Name", response.getBody().getDoctor_name());
    }

    @Test
    void testUpdateDoctor_Forbidden() {
        DoctorDTO inputDTO = new DoctorDTO();
        inputDTO.setDoctor_name("Updated Name");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("anotheruser");

        ResponseEntity<DoctorDTO> response = doctorController.updateDoctor(inputDTO, "doc123", auth);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testDeleteDoctorById() {
        doNothing().when(doctorService).deleteDoctorById(1L);

        ResponseEntity<String> response = doctorController.deleteDoctorById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("deleted successfully"));
    }
}
