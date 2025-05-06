package com.soprasteria.clinic.appointment.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.entity.Appointment;
import com.soprasteria.clinic.appointment.entity.Availability;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.InvalidTimeSlotException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.PatientNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.DoctorNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.UnauthorizedAccessException;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AppointmentRepository;
import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.repo.PatientRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger logger = LogManager.getLogger(AppointmentServiceImpl.class);

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private GlobalMapper globalMapper;

    @Override
    @Transactional
    public AppointmentDTO bookAppointment(AppointmentDTO appointmentDTO, Long patientId, Authentication authentication) {
        String loggedInUsername = authentication.getName();

        // Fetch the patient by ID
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient with ID '" + patientId + "' not found"));

        // Authorization Check: Ensure the logged-in user is the same as the patient with the provided ID
        if (!loggedInUsername.equals(patient.getUsername())) {
            logger.warn("Unauthorized booking attempt by: {}", loggedInUsername);
            throw new UnauthorizedAccessException("You are not authorized to book an appointment for another patient.");
        }

        // Proceed with the booking logic
        Doctor doctor = doctorRepository.findById(appointmentDTO.getDoctor().getDoctor_id())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + appointmentDTO.getDoctor().getDoctor_id()));

        LocalDate date = appointmentDTO.getAppointment_date();
        LocalTime startTime = appointmentDTO.getAppointment_startTime();
        LocalTime endTime = appointmentDTO.getAppointment_endTime();

        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new InvalidTimeSlotException("Invalid appointment time range.");
        }

        boolean free = availabilityRepository.isTimeSlotAvailable(doctor.getDoctor_id(), date, startTime, endTime);
        if (!free) {
            throw new InvalidTimeSlotException("The selected time slot is not available.");
        }

        boolean alreadyBooked = appointmentRepository.existsBookedAppointment(doctor.getDoctor_id(), date, startTime, endTime);
        if (alreadyBooked) {
            throw new InvalidTimeSlotException("Appointment already booked for the same slot.");
        }

        Appointment appointment = globalMapper.toAppointmentEntity(appointmentDTO, doctor, patient);
        appointment.setAppointment_status("Appointment_Booked");
        Appointment saved = appointmentRepository.save(appointment);

        // Adjust overlapping availabilities
        handleOverlappingAvailabilities(doctor, date, startTime, endTime);

        logger.info("Appointment booked successfully with ID: {}", saved.getAppointment_id());
        return globalMapper.toAppointmentDTO(saved);
    }

    private void handleOverlappingAvailabilities(Doctor doctor, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Availability> overlaps = availabilityRepository.findOverlappingAvailabilities(doctor.getDoctor_id(), date, startTime, endTime);

        for (Availability a : overlaps) {
            boolean hasSplit = false;

            // Before segment
            if (a.getAvailability_startTime().isBefore(startTime)) {
                LocalTime bs = a.getAvailability_startTime();
                LocalTime be = startTime;

                if (!availabilityRepository.existsByDoctorAndDateAndTime(doctor.getDoctor_id(), date, bs, be)) {
                    Availability before = new Availability();
                    before.setDoctor(doctor);
                    before.setAvailability_date(date);
                    before.setAvailability_startTime(bs);
                    before.setAvailability_endTime(be);
                    before.setAvailability_status("Available");
                    availabilityRepository.save(before);
                }
                hasSplit = true;
            }

            // After segment
            if (a.getAvailability_endTime().isAfter(endTime)) {
                LocalTime as = endTime;
                LocalTime ae = a.getAvailability_endTime();

                if (!availabilityRepository.existsByDoctorAndDateAndTime(doctor.getDoctor_id(), date, as, ae)) {
                    Availability after = new Availability();
                    after.setDoctor(doctor);
                    after.setAvailability_date(date);
                    after.setAvailability_startTime(as);
                    after.setAvailability_endTime(ae);
                    after.setAvailability_status("Available");
                    availabilityRepository.save(after);
                }
                hasSplit = true;
            }

            // Final handling: delete the original if it was split, else mark as booked
            if (hasSplit) {
                availabilityRepository.delete(a);
            } else {
                a.setAvailability_status("Booked");
                availabilityRepository.save(a);
            }
        }
    }

    @Override
    @Transactional
    public List<AppointmentDTO> viewAllAppointmentsForPatient(Long patientId, int page, int size, Authentication authentication) {
        String loginUsername = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isPatient = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PATIENT"));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));

        if (!(patient.getUsername().equals(loginUsername) || isAdmin)) {
            logger.warn("Unauthorized access attempt by user: {}", loginUsername);
            throw new UnauthorizedAccessException("You are not authorized to view these appointments.");
        }

        Page<Appointment> pageData = appointmentRepository.findAppointmentsByPatientId(patientId, PageRequest.of(page, size));
        return pageData.map(globalMapper::toAppointmentDTO).getContent();
    }

    @Override
    @Transactional
    public List<AppointmentDTO> viewAllAppointmentsForDoctor(Long doctorId, int page, int size, Authentication authentication) {
        String loginUsername = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_DOCTOR"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

        if (!(doctor.getUsername().equals(loginUsername) || isAdmin)) {
            logger.warn("Unauthorized access attempt by user: {}", loginUsername);
            throw new UnauthorizedAccessException("You are not authorized to view these appointments.");
        }

        Page<Appointment> pageData = appointmentRepository.findAppointmentsByDoctorId(doctorId, PageRequest.of(page, size));
        return pageData.map(globalMapper::toAppointmentDTO).getContent();
    }

    @Override
    @Transactional
    public List<AppointmentDTO> viewAllAppointments(int page, int size) {
        Page<Appointment> pageData = appointmentRepository.findAll(PageRequest.of(page, size));
        return pageData.map(globalMapper::toAppointmentDTO).getContent();
    }

    @Override
    @Transactional
    public AppointmentDTO rescheduleAppointment(AppointmentDTO appointmentDTO, Long appointmentId, String loggedInUsername) {
        // Fetch the existing appointment
        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

        // Verify if the logged-in user is the same as the patient who booked it
        Patient patient = existingAppointment.getPatient();
        if (!patient.getUsername().equals(loggedInUsername)) {
            logger.warn("Unauthorized reschedule attempt by: {}", loggedInUsername);
            throw new UnauthorizedAccessException("You are not authorized to reschedule this appointment.");
        }

        // Get new time and date
        LocalDate newDate = appointmentDTO.getAppointment_date();
        LocalTime newStartTime = appointmentDTO.getAppointment_startTime();
        LocalTime newEndTime = appointmentDTO.getAppointment_endTime();

        if (newStartTime == null || newEndTime == null || !newEndTime.isAfter(newStartTime)) {
            throw new InvalidTimeSlotException("Invalid reschedule time range.");
        }

        Doctor doctor = existingAppointment.getDoctor();

        // Check if the new slot is available
        boolean available = availabilityRepository.isTimeSlotAvailable(doctor.getDoctor_id(), newDate, newStartTime, newEndTime);
        boolean alreadyBooked = appointmentRepository.existsBookedAppointment(doctor.getDoctor_id(), newDate, newStartTime, newEndTime);
        if (!available || alreadyBooked) {
            throw new InvalidTimeSlotException("The selected reschedule time slot is not available.");
        }

        // Update the old time slot to Available
        Availability oldAvailability = new Availability();
        oldAvailability.setDoctor(doctor);
        oldAvailability.setAvailability_date(existingAppointment.getAppointment_date());
        oldAvailability.setAvailability_startTime(existingAppointment.getAppointment_startTime());
        oldAvailability.setAvailability_endTime(existingAppointment.getAppointment_endTime());
        oldAvailability.setAvailability_status("Available");
        availabilityRepository.save(oldAvailability);

        // Set new time and status for appointment
        existingAppointment.setAppointment_date(newDate);
        existingAppointment.setAppointment_startTime(newStartTime);
        existingAppointment.setAppointment_endTime(newEndTime);
        existingAppointment.setAppointment_status("Appointment_Rescheduled");
        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);

        // Adjust overlapping availabilities (same logic used in booking)
        handleOverlappingAvailabilities(doctor, newDate, newStartTime, newEndTime);

        logger.info("Appointment with ID {} successfully rescheduled", appointmentId);
        return globalMapper.toAppointmentDTO(updatedAppointment);
    }

    @Override
    @Transactional
    public String cancelAppointment(Long appointmentId, Long patientId, Authentication authentication) {
        String loggedInUsername = authentication.getName();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

        if (!appointment.getPatient().getPatient_id().equals(patientId)) {
            throw new UnauthorizedAccessException("You are not authorized to cancel this appointment.");
        }

        if (!appointment.getPatient().getUsername().equals(loggedInUsername)) {
            throw new UnauthorizedAccessException("You can only cancel your own appointments.");
        }

        appointment.setAppointment_status("Canceled");
        appointmentRepository.save(appointment);

        // Restore availability after cancellation
        updateAvailabilityForCanceledAppointment(appointment);

        return "Appointment canceled successfully.";
    }

    private void updateAvailabilityForCanceledAppointment(Appointment appointment) {
        LocalDate date = appointment.getAppointment_date();
        LocalTime startTime = appointment.getAppointment_startTime();
        LocalTime endTime = appointment.getAppointment_endTime();
        Long doctorId = appointment.getDoctor().getDoctor_id();

        Availability availability = availabilityRepository.findByDoctorAndDateAndTime(doctorId, date, startTime, endTime);

        if (availability != null) {
            availability.setAvailability_status("Available");
            availabilityRepository.save(availability);
        } else {
            Availability newAvailability = new Availability();
            newAvailability.setDoctor(appointment.getDoctor());
            newAvailability.setAvailability_date(date);
            newAvailability.setAvailability_startTime(startTime);
            newAvailability.setAvailability_endTime(endTime);
            newAvailability.setAvailability_status("Available");
            availabilityRepository.save(newAvailability);
        }
    }
}
