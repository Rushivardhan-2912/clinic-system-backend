package com.soprasteria.clinic.appointment.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.mapper.PatientMapper;
import com.soprasteria.clinic.appointment.repo.PatientRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient basePatient;

    @BeforeEach
    void setUp() {
        // Create a base patient for reuse
        basePatient = new Patient();
        basePatient.setUsername("john123");
        basePatient.setPatient_name("John Doe");
        basePatient.setPatient_email("john.doe@example.com");
        basePatient.setPatient_phoneNumber("1234567890");
    }

    @Test
    void testFindByUsername_Success() {
        when(patientRepository.findByUsername("john123"))
                .thenReturn(Optional.of(basePatient));

        Patient result = patientService.findByUsername("john123");

        assertNotNull(result);
        assertEquals("john123", result.getUsername());
        verify(patientRepository, times(1)).findByUsername("john123");
    }

    @Test
    void testFindByUsername_NotFound() {
        when(patientRepository.findByUsername("nope"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientService.findByUsername("nope")
        );

        assertTrue(ex.getMessage().contains("Patient not found with username: nope"));
    }

    @Test
    void testRegisterPatient() {
        when(patientRepository.save(any(Patient.class)))
                .thenReturn(basePatient);

        Patient saved = patientService.registerPatient(basePatient);

        assertNotNull(saved);
        assertEquals("john123", saved.getUsername());
        verify(patientRepository, times(1)).save(basePatient);
    }

    @Test
    void testGetAllPatientsPaginated() {
        Patient p2 = new Patient();
        p2.setUsername("jane456");
        p2.setPatient_name("Jane Roe");

        List<Patient> patients = Arrays.asList(basePatient, p2);
        Page<Patient> patientPage = new PageImpl<>(patients, PageRequest.of(0, 2), patients.size());

        when(patientRepository.findAll(any(Pageable.class)))
                .thenReturn(patientPage);

        // stub static mapper
        try (var mock = mockStatic(PatientMapper.class)) {
            mock.when(() -> PatientMapper.toDTO(basePatient))
                    .thenReturn(new PatientDTO() {{
                        setUsername("john123");
                        setPatient_name("John Doe");
                    }});
            mock.when(() -> PatientMapper.toDTO(p2))
                    .thenReturn(new PatientDTO() {{
                        setUsername("jane456");
                        setPatient_name("Jane Roe");
                    }});

            Page<PatientDTO> dtos = patientService.getAllPatientsPaginated(0, 2);
            assertEquals(2, dtos.getContent().size());
            assertEquals("john123", dtos.getContent().get(0).getUsername());
            assertEquals("jane456", dtos.getContent().get(1).getUsername());
        }
    }

    @Test
    void testUpdatePatient_Success() {
        PatientDTO update = new PatientDTO();
        update.setPatient_name("Johnny");

        when(patientRepository.findByUsername("john123"))
                .thenReturn(Optional.of(basePatient));
        when(patientRepository.save(any(Patient.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // stub mapper
        try (var mock = mockStatic(PatientMapper.class)) {
            mock.when(() -> PatientMapper.toDTO(any(Patient.class)))
                    .thenAnswer(inv -> {
                        Patient p = inv.getArgument(0);
                        PatientDTO dto = new PatientDTO();
                        dto.setUsername(p.getUsername());
                        dto.setPatient_name(p.getPatient_name());
                        return dto;
                    });

            PatientDTO result = patientService.updatePatient(update, "john123");
            assertEquals("Johnny", result.getPatient_name());
        }
    }

    @Test
    void testUpdatePatient_NotFound() {
        when(patientRepository.findByUsername("nope"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientService.updatePatient(new PatientDTO(), "nope")
        );
        assertTrue(ex.getMessage().contains("Patient not found with username: nope"));
    }

    @Test
    void testDeletePatientById_Success() {
        when(patientRepository.existsById(10L)).thenReturn(true);

        // should not throw
        assertDoesNotThrow(() -> patientService.deletePatientById(10L));
        verify(patientRepository, times(1)).deleteById(10L);
    }

    @Test
    void testDeletePatientById_NotFound() {
        when(patientRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientService.deletePatientById(99L)
        );
        assertTrue(ex.getMessage().contains("Patient with ID 99 not found"));
    }
}
