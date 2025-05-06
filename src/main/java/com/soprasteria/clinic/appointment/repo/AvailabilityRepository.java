package com.soprasteria.clinic.appointment.repo;

import com.soprasteria.clinic.appointment.entity.Doctor;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.soprasteria.clinic.appointment.entity.Availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    // Check if a time slot is available for a doctor
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Availability a " +
            "WHERE a.doctor.doctor_id = :doctorId " +
            "AND a.availability_date = :date " +
            "AND ((a.availability_startTime <= :startTime AND a.availability_endTime > :startTime) " +
            "OR (a.availability_startTime < :endTime AND a.availability_endTime >= :endTime))")
    boolean isTimeSlotAvailable(@Param("doctorId") Long doctorId,
                                @Param("date") LocalDate date,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Availability a " +
            "WHERE a.doctor.doctor_id = :doctorId " +
            "AND a.availability_date = :date " +
            "AND a.availability_startTime = :startTime " +
            "AND a.availability_endTime = :endTime")
    boolean existsByDoctorAndDateAndTime(@Param("doctorId") Long doctorId,
                                         @Param("date") LocalDate date,
                                         @Param("startTime") LocalTime startTime,
                                         @Param("endTime") LocalTime endTime);

    // Find overlapping availability slots for a doctor
    @Query("SELECT a FROM Availability a " +
            "WHERE a.doctor.doctor_id = :doctorId " +
            "AND (a.availability_startTime < :endTime " +
            "AND a.availability_endTime > :startTime)")
    List<Availability> findOverlappingAvailabilities(@Param("doctorId") Long doctorId,
                                                     @Param("date") LocalDate date,
                                                     @Param("startTime") LocalTime startTime,
                                                     @Param("endTime") LocalTime endTime);

    // Update availability status for a specific time slot
    @Modifying
    @Query("UPDATE Availability a " +
            "SET a.availability_status = :status, a.availability_date = :date " +
            "WHERE a.doctor.doctor_id = :doctorId " +
            "AND a.availability_startTime = :startTime " +
            "AND a.availability_endTime = :endTime")
    int updateAvailability(@Param("doctorId") Long doctorId,
                           @Param("startTime") LocalTime startTime,
                           @Param("endTime") LocalTime endTime,
                           @Param("date") LocalDate date,
                           @Param("status") String status);

    // Find availability by doctor, date, and exact time slot
    @Query("SELECT a FROM Availability a " +
            "WHERE a.doctor.doctor_id = :doctorId " +
            "AND a.availability_date = :appointmentDate " +
            "AND a.availability_startTime = :startTime " +
            "AND a.availability_endTime = :endTime")
    Availability findByDoctorAndDateAndTime(@Param("doctorId") Long doctorId,
                                            @Param("appointmentDate") LocalDate appointmentDate,
                                            @Param("startTime") LocalTime startTime,
                                            @Param("endTime") LocalTime endTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM Availability e WHERE e.availability_date < :today")
    void deletePastData(@Param("today") LocalDate today);

}