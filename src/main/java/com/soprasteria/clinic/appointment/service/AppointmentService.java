package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AppointmentService {
    AppointmentDTO bookAppointment(AppointmentDTO appointmentDTO, Long id,Authentication authentication);
    List<AppointmentDTO> viewAllAppointmentsForPatient(Long patientId, int page, int size,Authentication authentication);
    List<AppointmentDTO> viewAllAppointmentsForDoctor(Long doctorId, int page, int size,Authentication authentication);
    List<AppointmentDTO> viewAllAppointments(int page, int size);
    String cancelAppointment(Long appointmentId, Long patientId,Authentication authentication);
    AppointmentDTO rescheduleAppointment(AppointmentDTO appointmentDTO, Long appointmentId, String loggedInUsername);
}
