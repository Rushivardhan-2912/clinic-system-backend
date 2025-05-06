package com.soprasteria.clinic.appointment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ClinicExceptionHandler {

    // --- Exception Handlers ---

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<?> handlePatientNotFound(PatientNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<?> handleAdminNotFound(AdminNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<?> handleDoctorNotFound(DoctorNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<?> handleAppointmentNotFound(AppointmentNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidTimeSlotException.class)
    public ResponseEntity<?> handleInvalidTimeSlot(InvalidTimeSlotException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AvailabilityNotFoundException.class)
    public ResponseEntity<?> handleAvailabilityNotFound(AvailabilityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<?> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllOtherExceptions(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage());
    }

    private ResponseEntity<?> buildResponse(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return new ResponseEntity<>(error, status);
    }

    // --- Custom Exception Classes ---

    public static class PatientNotFoundException extends RuntimeException {
        public PatientNotFoundException(String message) {
            super(message);
        }
    }

    public static class DoctorNotFoundException extends RuntimeException {
        public DoctorNotFoundException(String message) {
            super(message);
        }
    }

    public static class AppointmentNotFoundException extends RuntimeException {
        public AppointmentNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidTimeSlotException extends RuntimeException {
        public InvalidTimeSlotException(String message) {
            super(message);
        }
    }

    public static class AvailabilityNotFoundException extends RuntimeException {
        public AvailabilityNotFoundException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedAccessException extends RuntimeException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }

    public static class AdminNotFoundException extends RuntimeException {
        public AdminNotFoundException(String message) {
            super(message);
        }
    }

}
