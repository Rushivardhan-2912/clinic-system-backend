package com.soprasteria.clinic.appointment.dto;

public class PatientDTO {
	
	private Long patient_id;
	private String patient_name;
	private String patient_email;
	private String patient_phoneNumber;
	private String patient_age;
	private String username;

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
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
}