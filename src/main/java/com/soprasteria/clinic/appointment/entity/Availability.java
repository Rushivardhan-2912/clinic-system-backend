package com.soprasteria.clinic.appointment.entity;

import java.time.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "availability_seq")
    @SequenceGenerator(name = "availability_seq", sequenceName = "availability_sequence", allocationSize = 1)
    private Long availability_id;

    @NotNull(message = "Availability date cannot be null")
    @FutureOrPresent(message = "Availability date must be today or future")
    private LocalDate availability_date;

    @NotNull(message = "Availability start time cannot be null")
    private LocalTime availability_startTime;

    @NotNull(message = "Availability end time cannot be null")
    private LocalTime availability_endTime;


    private String availability_status;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @JsonBackReference
    private Doctor doctor;

    public String getAvailability_status() {
		return availability_status;
	}

	public void setAvailability_status(String availability_status) {
		this.availability_status = availability_status;
	}

    // Getters and Setters

    public Long getAvailability_id() {
        return availability_id;
    }

    public void setAvailability_id(Long availability_id) {
        this.availability_id = availability_id;
    }

    public LocalDate getAvailability_date() {
        return availability_date;
    }

    public void setAvailability_date(LocalDate availability_date) {
        this.availability_date = availability_date;
    }

    public LocalTime getAvailability_startTime() {
        return availability_startTime;
    }

    public void setAvailability_startTime(LocalTime availability_startTime) {
        this.availability_startTime = availability_startTime;
    }

    public LocalTime getAvailability_endTime() {
        return availability_endTime;
    }

    public void setAvailability_endTime(LocalTime availability_endTime) {
        this.availability_endTime = availability_endTime;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

}
