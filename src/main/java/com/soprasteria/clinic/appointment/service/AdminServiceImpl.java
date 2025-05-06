package com.soprasteria.clinic.appointment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.soprasteria.clinic.appointment.entity.Admin;
import com.soprasteria.clinic.appointment.repo.AdminRepository;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LogManager.getLogger(AdminServiceImpl.class);

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public Admin findByUsername(String username) {
        logger.info("Loading admin with username: {}", username);
        Admin admin = adminRepository.findByUsername(username);
        return admin;
    }

}
