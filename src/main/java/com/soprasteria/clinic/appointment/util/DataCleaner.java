package com.soprasteria.clinic.appointment.util;

import com.soprasteria.clinic.appointment.repo.AppointmentRepository;
import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataCleaner implements CommandLineRunner {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public void run(String... args) {
        LocalDate today = LocalDate.now();
        availabilityRepository.deletePastData(today);
        appointmentRepository.deletePastData(today);
        System.out.println("Old data deleted on startup.");
    }
}
