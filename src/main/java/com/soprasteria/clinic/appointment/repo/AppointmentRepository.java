package com.soprasteria.clinic.appointment.repo;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.soprasteria.clinic.appointment.entity.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	Page<Appointment> findAll(Pageable pageable);

	@Query("SELECT a FROM Appointment a WHERE a.patient.patient_id = :patientId")
	Page<Appointment> findAppointmentsByPatientId(Long patientId, Pageable pageable);

	@Query("SELECT a FROM Appointment a WHERE a.doctor.doctor_id = :doctorId")
	Page<Appointment> findAppointmentsByDoctorId(Long doctorId, Pageable pageable);

	@Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE " +
			"a.doctor.doctor_id = :doctorId AND " +
			"a.appointment_date = :date AND " +
			"(a.appointment_startTime < :endTime AND a.appointment_endTime > :startTime) AND " +
			"a.appointment_status = 'Appointment_Booked'")
	boolean existsBookedAppointment(@Param("doctorId") Long doctorId,
									@Param("date") LocalDate date,
									@Param("startTime") LocalTime startTime,
									@Param("endTime") LocalTime endTime);

	@Modifying
	@Transactional
	@Query("DELETE FROM Appointment a WHERE a.appointment_date < :today")
	void deletePastData(@Param("today") LocalDate today);
}
