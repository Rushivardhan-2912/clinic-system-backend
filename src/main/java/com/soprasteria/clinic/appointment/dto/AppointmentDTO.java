package com.soprasteria.clinic.appointment.dto;


import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentDTO {
	
	private Long appointment_id;
    private LocalDate appointment_date;
    private LocalTime appointment_startTime;
    private LocalTime appointment_endTime;
    private String appointment_status;
    
    private PatientDTO patient;
    private DoctorDTO doctor;
    
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
	public PatientDTO getPatient() {
		return patient;
	}
	public void setPatient(PatientDTO patient) {
		this.patient = patient;
	}
	public DoctorDTO getDoctor() {
		return doctor;
	}
	public void setDoctor(DoctorDTO doctor) {
		this.doctor = doctor;
	}
}