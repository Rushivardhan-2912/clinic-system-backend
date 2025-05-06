package com.soprasteria.clinic.appointment.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Availability;
import com.soprasteria.clinic.appointment.entity.Appointment;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.entity.Patient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlobalMapper {

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LogManager.getLogger(GlobalMapper.class);

    // PATIENT
    public Patient toPatientEntity(PatientDTO dto){
        logger.debug("Converting PatientDTO to entity: {}", dto);
        Patient patient = objectMapper.convertValue(dto, Patient.class);
        logger.debug("Converted to Patient entity: {}", patient);
        return patient;
    }

    public PatientDTO toPatientDTO(Patient patient){
        logger.debug("Converting Patient entity to DTO: {}", patient);
        PatientDTO dto = objectMapper.convertValue(patient, PatientDTO.class);
        logger.debug("Converted to PatientDTO: {}", dto);
        return dto;
    }

    // DOCTOR
    public Doctor toDoctorEntity(DoctorDTO dto){
        logger.debug("Converting DoctorDTO to entity: {}", dto);
        Doctor doctor = objectMapper.convertValue(dto, Doctor.class);
        logger.debug("Converted to Doctor entity: {}", doctor);
        return doctor;
    }

    public DoctorDTO toDoctorDTO(Doctor doctor){
        logger.debug("Converting Doctor entity to DTO: {}", doctor);
        DoctorDTO dto = objectMapper.convertValue(doctor, DoctorDTO.class);
        logger.debug("Converted to DoctorDTO: {}", dto);
        return dto;
    }

    // AVAILABILITY
    public AvailabilityDTO toAvailabilityDTO(Availability availability) {
        logger.debug("Converting Availability entity to DTO: {}", availability);
        AvailabilityDTO dto = objectMapper.convertValue(availability, AvailabilityDTO.class);
        if (availability.getDoctor() != null) {
            dto.setDoctor(toDoctorDTO(availability.getDoctor()));
        }
        logger.debug("Converted to AvailabilityDTO: {}", dto);
        return dto;
    }

    public Availability toAvailabilityEntity(AvailabilityDTO dto, Doctor doctor) {
        logger.debug("Converting AvailabilityDTO to entity: {}", dto);
        Availability availability = objectMapper.convertValue(dto, Availability.class);
        availability.setDoctor(doctor);
        logger.debug("Converted to Availability entity: {}", availability);
        return availability;
    }

    // APPOINTMENT
    public AppointmentDTO toAppointmentDTO(Appointment appointment) {
        logger.debug("Converting Appointment entity to DTO: {}", appointment);
        AppointmentDTO dto = objectMapper.convertValue(appointment, AppointmentDTO.class);
        if (appointment.getDoctor() != null) {
            dto.setDoctor(toDoctorDTO(appointment.getDoctor()));
        }
        if (appointment.getPatient() != null) {
            dto.setPatient(toPatientDTO(appointment.getPatient()));
        }
        logger.debug("Converted to AppointmentDTO: {}", dto);
        return dto;
    }

    public Appointment toAppointmentEntity(AppointmentDTO dto, Doctor doctor, Patient patient) {
        logger.debug("Converting AppointmentDTO to entity: {}", dto);
        Appointment appointment = objectMapper.convertValue(dto, Appointment.class);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        logger.debug("Converted to Appointment entity: {}", appointment);
        return appointment;
    }

}