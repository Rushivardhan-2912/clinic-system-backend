package com.soprasteria.clinic.appointment.dto;


public class DoctorDTO {

	private Long doctor_id;
    private String doctor_name;
    private String doctor_specialization;
    private String username;

	public Long getDoctor_id() {
		return doctor_id;
	}
	public void setDoctor_id(Long doctor_id) {
		this.doctor_id = doctor_id;
	}
	public String getDoctor_name() {
		return doctor_name;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
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
	
}