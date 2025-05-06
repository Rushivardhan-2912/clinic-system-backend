package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import com.soprasteria.clinic.appointment.entity.Availability;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.mapper.AvailabilityMapper;
import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Doctor doctor;
    private Availability availability;
    private AvailabilityDTO availabilityDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        doctor = new Doctor();
        doctor.setDoctor_id(100L);
        doctor.setUsername("docUser");

        availability = new Availability();
        availability.setAvailability_id(1L);
        availability.setAvailability_date(LocalDate.of(2025, 5, 1));
        availability.setAvailability_startTime(LocalTime.of(9, 0));
        availability.setAvailability_endTime(LocalTime.of(11, 0));
        availability.setDoctor(doctor);

        availabilityDTO = new AvailabilityDTO();
        availabilityDTO.setAvailability_id(1L);
        availabilityDTO.setAvailability_date(LocalDate.of(2025, 5, 1));
        availabilityDTO.setAvailability_startTime(LocalTime.of(9, 0));
        availabilityDTO.setAvailability_endTime(LocalTime.of(11, 0));
        availabilityDTO.setDoctor(new com.soprasteria.clinic.appointment.dto.DoctorDTO() {{
            setDoctor_id(100L);
            setUsername("docUser");
        }});
    }

    @Test
    void testAddAvailability_Success() {
        when(doctorRepository.findByUsername("docUser")).thenReturn(Optional.of(doctor));
        try (var mock = mockStatic(AvailabilityMapper.class)) {
            mock.when(() -> AvailabilityMapper.toEntity(availabilityDTO, doctor)).thenReturn(availability);
            mock.when(() -> AvailabilityMapper.toDTO(availability)).thenReturn(availabilityDTO);

            when(availabilityRepository.save(availability)).thenReturn(availability);

            AvailabilityDTO result = availabilityService.addAvailability(availabilityDTO, "docUser");

            assertNotNull(result);
            assertEquals(availabilityDTO.getAvailability_id(), result.getAvailability_id());
            verify(availabilityRepository, times(1)).save(availability);
        }
    }

    @Test
    void testGetAllAvailabilities_Paginated() {
        // Prepare mock paginated data
        int page = 0;
        int size = 3;
        PageRequest pageRequest = PageRequest.of(page, size);

        // Simulate paginated data
        Page<Availability> pageEntity = new PageImpl<>(List.of(availability, availability, availability), pageRequest, 5L); // 5 items in total

        when(availabilityRepository.findAll(pageRequest)).thenReturn(pageEntity);

        try (var mock = mockStatic(AvailabilityMapper.class)) {
            mock.when(() -> AvailabilityMapper.toDTO(availability)).thenReturn(availabilityDTO);

            // Call the service method with pagination
            List<AvailabilityDTO> result = availabilityService.getAllAvailabilities(page, size);

            // Assert that the results match the expected pagination
            assertEquals(3, result.size()); // 3 items per page
            assertEquals(availabilityDTO.getAvailability_id(), result.get(0).getAvailability_id());
            verify(availabilityRepository, times(1)).findAll(pageRequest); // Verify that the repository was called with the correct pageable
        }
    }

    @Test
    void testUpdateAvailability_Success() {
        when(doctorRepository.findByUsername("docUser")).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.save(any(Availability.class))).thenReturn(availability);

        availabilityDTO.setAvailability_status("Available"); // status irrelevant to mapper
        try (var mock = mockStatic(AvailabilityMapper.class)) {
            mock.when(() -> AvailabilityMapper.toDTO(availability)).thenReturn(availabilityDTO);

            AvailabilityDTO updated = availabilityService.updateAvailability(availabilityDTO, "docUser");

            assertNotNull(updated);
            assertEquals(availabilityDTO.getAvailability_id(), updated.getAvailability_id());
        }
    }

    @Test
    void testUpdateAvailability_NotFoundDoctor() {
        when(doctorRepository.findByUsername("docUser")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                availabilityService.updateAvailability(availabilityDTO, "docUser")
        );
        assertTrue(ex.getMessage().contains("Doctor with username"));
    }

    @Test
    void testDeleteAvailability_Success() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(doctorRepository.findByUsername("docUser")).thenReturn(Optional.of(doctor));

        assertDoesNotThrow(() -> availabilityService.deleteAvailability(1L, "docUser"));
        verify(availabilityRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteAvailability_NotFound() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.empty());
        when(doctorRepository.findByUsername("docUser")).thenReturn(Optional.of(doctor));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                availabilityService.deleteAvailability(1L, "docUser")
        );
        assertTrue(ex.getMessage().contains("Availability not found"));
    }

    @Test
    void testDeleteAvailability_Unauthorized() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        Doctor other = new Doctor(); other.setDoctor_id(999L);
        when(doctorRepository.findByUsername("docUser")).thenReturn(Optional.of(other));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                availabilityService.deleteAvailability(1L, "docUser")
        );
        assertTrue(ex.getMessage().contains("not authorized"));
    }
}
