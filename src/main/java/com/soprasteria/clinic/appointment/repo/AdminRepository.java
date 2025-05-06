package com.soprasteria.clinic.appointment.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soprasteria.clinic.appointment.entity.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
	Admin findByUsername(String username);
}
