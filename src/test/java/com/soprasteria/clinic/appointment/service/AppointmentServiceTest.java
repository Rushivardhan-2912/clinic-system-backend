package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.*;
import com.soprasteria.clinic.appointment.mapper.AppointmentMapper;
import com.soprasteria.clinic.appointment.repo.*;

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

class AppointmentServiceTest {

    @Mock private DoctorRepository doctorRepo;
    @Mock private PatientRepository patientRepo;
    @Mock private AppointmentRepository appointmentRepo;
    @Mock private AvailabilityRepository availabilityRepo;

    @InjectMocks private AppointmentService service;

    private Patient patient;
    private Doctor doctor;
    private AppointmentDTO dto;
    private Appointment appt;
    private Availability avail;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        patient = new Patient();
        patient.setPatient_id(1L);
        patient.setUsername("pat");

        doctor = new Doctor();
        doctor.setDoctor_id(2L);

        dto = new AppointmentDTO();
        dto.setAppointment_id(100L);
        dto.setAppointment_date(LocalDate.of(2025,5,1));
        dto.setAppointment_startTime(LocalTime.of(9,0));
        dto.setAppointment_endTime(LocalTime.of(10,0));
        dto.setDoctor(new DoctorDTO() {{ setDoctor_id(2L); }});
        dto.setPatient(new PatientDTO() {{ setPatient_id(1L); }});

        appt = new Appointment();
        appt.setAppointment_id(100L);
        appt.setAppointment_date(dto.getAppointment_date());
        appt.setAppointment_startTime(dto.getAppointment_startTime());
        appt.setAppointment_endTime(dto.getAppointment_endTime());
        appt.setDoctor(doctor);
        appt.setPatient(patient);

        avail = new Availability();
        avail.setAvailability_id(50L);
        avail.setAvailability_date(dto.getAppointment_date());
        avail.setAvailability_startTime(dto.getAppointment_startTime());
        avail.setAvailability_endTime(dto.getAppointment_endTime());
        avail.setDoctor(doctor);
    }

    @Test
    void testBookAppointment_Success() {
        when(patientRepo.findByUsername("pat")).thenReturn(Optional.of(patient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(doctor));
        when(availabilityRepo.isTimeSlotAvailable(2L, dto.getAppointment_date(),
                dto.getAppointment_startTime(), dto.getAppointment_endTime()))
                .thenReturn(true);
        when(appointmentRepo.existsBookedAppointment(2L, dto.getAppointment_date(),
                dto.getAppointment_startTime(), dto.getAppointment_endTime()))
                .thenReturn(false);

        try (var m = mockStatic(AppointmentMapper.class)) {
            m.when(() -> AppointmentMapper.toEntity(dto, doctor, patient)).thenReturn(appt);
            m.when(() -> AppointmentMapper.toDTO(appt)).thenReturn(dto);

            when(appointmentRepo.save(appt)).thenReturn(appt);
            when(availabilityRepo.findOverlappingAvailabilities(2L,
                    dto.getAppointment_startTime(), dto.getAppointment_endTime()))
                    .thenReturn(List.of(avail));

            service.bookAppointment(dto, "pat");

            verify(appointmentRepo).save(appt);
            verify(availabilityRepo).save(avail);
        }
    }

    @Test
    void testBookAppointment_InvalidTime() {
        dto.setAppointment_endTime(LocalTime.of(8,0)); // before start
        when(patientRepo.findByUsername("pat")).thenReturn(Optional.of(patient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(doctor));
        assertThrows(RuntimeException.class,
                () -> service.bookAppointment(dto, "pat"));
    }

    @Test
    void testBookAppointment_SlotUnavailable() {
        when(patientRepo.findByUsername("pat")).thenReturn(Optional.of(patient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(doctor));
        when(availabilityRepo.isTimeSlotAvailable(anyLong(), any(), any(), any()))
                .thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> service.bookAppointment(dto, "pat"));
    }

    @Test
    void testBookAppointment_AlreadyBooked() {
        when(patientRepo.findByUsername("pat")).thenReturn(Optional.of(patient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(doctor));
        when(availabilityRepo.isTimeSlotAvailable(anyLong(), any(), any(), any()))
                .thenReturn(true);
        when(appointmentRepo.existsBookedAppointment(anyLong(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> service.bookAppointment(dto, "pat"));
    }

    @Test
    void testCancelAppointment_Success() {
        when(appointmentRepo.findById(100L)).thenReturn(Optional.of(appt));
        when(appointmentRepo.save(any(Appointment.class))).thenReturn(appt);
        when(availabilityRepo.findByDoctorAndDateAndTime(2L,
                dto.getAppointment_date(), dto.getAppointment_startTime(), dto.getAppointment_endTime()))
                .thenReturn(avail);

        String result = service.cancelAppointment(100L, 1L);
        assertEquals("Appointment canceled successfully.", result);
        verify(appointmentRepo).save(appt);
        verify(availabilityRepo).save(avail);
    }

    @Test
    void testCancelAppointment_NotFound() {
        when(appointmentRepo.findById(100L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.cancelAppointment(100L, 1L));
    }

    @Test
    void testCancelAppointment_Unauthorized() {
        appt.getPatient().setPatient_id(99L);
        when(appointmentRepo.findById(100L)).thenReturn(Optional.of(appt));
        assertThrows(RuntimeException.class,
                () -> service.cancelAppointment(100L, 1L));
    }

    @Test
    void testCancelAppointment_CreateNewAvail() {
        when(appointmentRepo.findById(100L)).thenReturn(Optional.of(appt));
        when(appointmentRepo.save(any(Appointment.class))).thenReturn(appt);
        when(availabilityRepo.findByDoctorAndDateAndTime(2L,
                dto.getAppointment_date(), dto.getAppointment_startTime(), dto.getAppointment_endTime()))
                .thenReturn(null);

        String result = service.cancelAppointment(100L, 1L);
        assertEquals("Appointment canceled successfully.", result);
        verify(availabilityRepo).save(any(Availability.class));
    }

    @Test
    void testViewAllAppointmentsForPatient_Paginated() {
        // Prepare a Page<Appointment>
        PageRequest pr = PageRequest.of(0, 5); // First page (0-based index), 5 items per page
        Page<Appointment> pageEntity = new PageImpl<>(List.of(appt), pr, 1L); // Mock a page with one element (total 1 element)

        // Mock the repository call to return the page
        when(appointmentRepo.findAppointmentsByPatientId(1L, pr)).thenReturn(pageEntity);
        // Mock the static method before calling the service method
        try (var m = mockStatic(AppointmentMapper.class)) {
            // Mock the mapping of Appointment to AppointmentDTO
            m.when(() -> AppointmentMapper.toDTO(appt)).thenReturn(dto);
            // Call the service method
            Page<AppointmentDTO> result = service.viewAllAppointmentsForPatient(1L, 0, 5); // Request the first page
            // Verify results
            assertEquals(1, result.getTotalElements()); // Only one element in the page
            assertEquals(dto, result.getContent().get(0)); // The DTO should match the mock

            // Verify that the repository method was called
            verify(appointmentRepo).findAppointmentsByPatientId(1L, pr);
        }
    }

    @Test
    void testViewAllAppointmentsForDoctor_Paginated() {
        // Prepare a Page<Appointment> with more than 3 items to test the third page
        PageRequest pr = PageRequest.of(0, 3); // Third page (index 2), 3 items per page
        Page<Appointment> pageEntity = new PageImpl<>(List.of(appt, appt, appt, appt, appt), pr, 5L); // Mock 5 items in total
        when(appointmentRepo.findAppointmentsByDoctorId(1L, pr)).thenReturn(pageEntity);
        // Mock the static method before calling the service method
        try (var m = mockStatic(AppointmentMapper.class)) {
            // Mock the mapping of Appointment to AppointmentDTO
            m.when(() -> AppointmentMapper.toDTO(appt)).thenReturn(dto);
            // Call the service method
            Page<AppointmentDTO> result = service.viewAllAppointmentsForDoctor(1L, 0, 3); // Request the third page (page index 2)
            // Verify results
            assertEquals(5, result.getTotalElements());
            assertEquals(dto, result.getContent().get(0));
            // Verify repository interaction
            verify(appointmentRepo).findAppointmentsByDoctorId(1L, pr);
        }
    }

    @Test
    void testViewAllAppointments_Paginated() {
        PageRequest pr = PageRequest.of(0, 10);
        Page<Appointment> pageEntity = new PageImpl<>(List.of(appt), pr, 1L);
        when(appointmentRepo.findAll(pr)).thenReturn(pageEntity);

        try (var mocks = mockStatic(AppointmentMapper.class)) {
            mocks.when(() -> AppointmentMapper.toDTO(appt)).thenReturn(dto);
            Page<AppointmentDTO> result = service.viewAllAppointments(0, 10);
            assertEquals(1, result.getTotalElements());
            assertEquals(dto, result.getContent().get(0));
            verify(appointmentRepo).findAll(pr);
        }
    }
}
