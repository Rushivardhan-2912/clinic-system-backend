package com.soprasteria.clinic.appointment.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doctor_seq")
    @SequenceGenerator(name = "doctor_seq", sequenceName = "doctor_sequence", allocationSize = 1)
    private Long doctor_id;

    @NotBlank(message = "Name is mandatory")
    private String doctor_name;

    @NotBlank(message = "Specialization is mandatory")
    private String doctor_specialization;

    @NotBlank(message = "Username is mandatory")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Password is mandatory")
    private String doctor_password;

    private String role = "DOCTOR";

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // One-to-many relationship with Availability
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Availability> doctor_availabilities;

    // One-to-many relationship with Appointment
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    @JsonManagedReference("appointment-doctor")  // Same unique name as in Appointment entity
    private List<Appointment> doctor_appointments;

    // Getters and Setters
    public Long getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(Long doctor_id) {
        this.doctor_id = doctor_id;
    }

    public String getDoctor_name() {
        return doctor_name;
    }

    public void setDoctor_name(String doctor_name) {
        this.doctor_name = doctor_name;
    }

    public String getDoctor_specialization() {
        return doctor_specialization;
    }

    public void setDoctor_specialization(String doctor_specialization) {
        this.doctor_specialization = doctor_specialization;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDoctor_password() {
        return doctor_password;
    }

    public void setDoctor_password(String doctor_password) {
        this.doctor_password = doctor_password;
    }

    public List<Availability> getDoctor_availabilities() {
        return doctor_availabilities;
    }

    public void setDoctor_availabilities(List<Availability> doctor_availabilities) {
        this.doctor_availabilities = doctor_availabilities;
    }

    public List<Appointment> getDoctor_appointments() {
        return doctor_appointments;
    }

    public void setDoctor_appointments(List<Appointment> doctor_appointments) {
        this.doctor_appointments = doctor_appointments;
    }
}
