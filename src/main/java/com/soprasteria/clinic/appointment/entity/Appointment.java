package com.soprasteria.clinic.appointment.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appointment_seq")
    @SequenceGenerator(name = "appointment_seq", sequenceName = "appointment_sequence", allocationSize = 1)
    private Long appointment_id;

    @NotNull(message = "Appointment date cannot be null")
    @FutureOrPresent(message = "Appointment date must be today or future")
    private LocalDate appointment_date;

    @NotNull(message = "Start time cannot be null")
    private LocalTime appointment_startTime;

    @NotNull(message = "End time cannot be null")
    private LocalTime appointment_endTime;

    @NotBlank(message = "Status is mandatory")
    private String appointment_status;

    // Many-to-one relationship with Patient
    @ManyToOne
    @JoinColumn(name = "patient_id")
    @JsonBackReference("appointment-patient")  // Unique name for patient reference
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @JsonBackReference("appointment-doctor")  // Unique name for doctor reference
    private Doctor doctor;

    // Getters and Setters
    public Long getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(Long appointment_id) {
        this.appointment_id = appointment_id;
    }

    public LocalDate getAppointment_date() {
        return appointment_date;
    }

    public void setAppointment_date(LocalDate appointment_date) {
        this.appointment_date = appointment_date;
    }

    public LocalTime getAppointment_startTime() {
        return appointment_startTime;
    }

    public void setAppointment_startTime(LocalTime appointment_startTime) {
        this.appointment_startTime = appointment_startTime;
    }

    public LocalTime getAppointment_endTime() {
        return appointment_endTime;
    }

    public void setAppointment_endTime(LocalTime appointment_endTime) {
        this.appointment_endTime = appointment_endTime;
    }

    public String getAppointment_status() {
        return appointment_status;
    }

    public void setAppointment_status(String appointment_status) {
        this.appointment_status = appointment_status;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}
