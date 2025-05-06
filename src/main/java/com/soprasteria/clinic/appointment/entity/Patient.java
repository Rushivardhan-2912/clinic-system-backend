package com.soprasteria.clinic.appointment.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_seq")
    @SequenceGenerator(name = "patient_seq", sequenceName = "patient_sequence", allocationSize = 1)
    private Long patient_id;

    @NotBlank(message = "Patient name is required")
    private String patient_name;

    @NotBlank(message = "Patient email is required")
    @Column(unique = true)
    @Email(message = "Please provide a valid email address")
    private String patient_email;

    @NotBlank(message = "Patient phone number is required")
    @Column(unique = true)
    @Pattern(regexp = "^[0-9]{10}$", message = "Please provide a valid phone number")
    @Size(min = 10, max = 10, message = "Please provide a valid phone number")
    private String patient_phoneNumber;

    @NotBlank(message = "Age is mandatory")
    @Pattern(regexp = "^\\d{1,2}$", message = "Age must be between 1 and 2 digits")
    private String patient_age;

    @NotBlank(message = "Patient username is required")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Password is mandatory")
    private String patient_password;

    private String role = "PATIENT";

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // One-to-many relationship with Appointment
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonManagedReference("appointment-patient")  // Same unique name as in Appointment entity
    private List<Appointment> patient_appointments;


    // Getters and Setters
    public Long getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Long patient_id) {
        this.patient_id = patient_id;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public String getPatient_email() {
        return patient_email;
    }

    public void setPatient_email(String patient_email) {
        this.patient_email = patient_email;
    }

    public String getPatient_phoneNumber() {
        return patient_phoneNumber;
    }

    public void setPatient_phoneNumber(String patient_phoneNumber) {
        this.patient_phoneNumber = patient_phoneNumber;
    }

    public String getPatient_age() {
        return patient_age;
    }

    public void setPatient_age(String patient_age) {
        this.patient_age = patient_age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPatient_password() {
        return patient_password;
    }

    public void setPatient_password(String patient_password) {
        this.patient_password = patient_password;
    }

    public List<Appointment> getPatient_appointments() {
        return patient_appointments;
    }

    public void setPatient_appointments(List<Appointment> patient_appointments) {
        this.patient_appointments = patient_appointments;
    }
}
