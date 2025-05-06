package com.soprasteria.clinic.appointment.dto;


import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityDTO {

	private Long availability_id;
    private LocalDate availability_date;
    private LocalTime availability_startTime;
    private LocalTime availability_endTime;
    private String availability_status;
    
    public String getAvailability_status() {
		return availability_status;
	}

	public void setAvailability_status(String availability_status) {
		this.availability_status = availability_status;
	}

	private DoctorDTO doctor;

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

	public DoctorDTO getDoctor() {
		return doctor;
	}

	public void setDoctor(DoctorDTO doctor) {
		this.doctor = doctor;
	}
  
}