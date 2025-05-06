package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.entity.Admin;

public interface AdminService {
    Admin findByUsername(String username);
}
