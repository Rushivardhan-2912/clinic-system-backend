package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.mapper.DoctorMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;
    private DoctorDTO doctorDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        doctor = new Doctor();
        doctor.setDoctor_id(1L);
        doctor.setDoctor_name("Dr. Strange");
        doctor.setDoctor_specialization("Magic");
        doctor.setUsername("strange");

        doctorDTO = new DoctorDTO();
        doctorDTO.setDoctor_id(1L);
        doctorDTO.setDoctor_name("Dr. Strange");
        doctorDTO.setDoctor_specialization("Magic");
        doctorDTO.setUsername("strange");
    }

    @Test
    void testRegisterDoctor() {
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        Doctor saved = doctorService.registerDoctor(doctor);

        assertNotNull(saved);
        assertEquals("Dr. Strange", saved.getDoctor_name());
        verify(doctorRepository, times(1)).save(doctor);
    }

    @Test
    void testGetAllDoctors() {
        // Pagination setup
        int page = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(page, size);

        // Create a paginated result
        Page<Doctor> doctorsPage = new PageImpl<>(List.of(doctor), pageRequest, 1);

        when(doctorRepository.findAll(pageRequest)).thenReturn(doctorsPage);

        // Test the method
        Page<DoctorDTO> doctorDTOPage = doctorService.getAllDoctorsPaginated(page, size);

        // Assert the results
        assertEquals(1, doctorDTOPage.getContent().size());
        assertEquals("Dr. Strange", doctorDTOPage.getContent().get(0).getDoctor_name());
        verify(doctorRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void testUpdateDoctor() {
        when(doctorRepository.findByUsername("strange")).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        DoctorDTO updateDTO = new DoctorDTO();
        updateDTO.setDoctor_name("Dr. Strange Updated");

        DoctorDTO updatedDoctor = doctorService.updateDoctor(updateDTO, "strange");

        assertNotNull(updatedDoctor);
        assertEquals("Dr. Strange Updated", updatedDoctor.getDoctor_name());
        verify(doctorRepository, times(1)).findByUsername("strange");
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    void testUpdateDoctor_NotFound() {
        when(doctorRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        DoctorDTO updateDTO = new DoctorDTO();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDoctor(updateDTO, "unknown");
        });

        assertEquals("Doctor not found", exception.getMessage());
    }

    @Test
    void testDeleteDoctorById() {
        when(doctorRepository.existsById(1L)).thenReturn(true);

        doctorService.deleteDoctorById(1L);

        verify(doctorRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteDoctorById_NotFound() {
        when(doctorRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.deleteDoctorById(99L);
        });

        assertEquals("Doctor with ID 99 not found", exception.getMessage());
    }

    @Test
    void testFindByUsername() {
        when(doctorRepository.findByUsername("strange")).thenReturn(Optional.of(doctor));

        Optional<Doctor> found = doctorService.findByUsername("strange");

        assertTrue(found.isPresent());
        assertEquals("Dr. Strange", found.get().getDoctor_name());
    }
}
