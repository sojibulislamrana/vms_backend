package com.sojibulislamrana.vms_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a vehicle requested by ID or vehicleNumber is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(Long id) {
        super(String.format("Vehicle with ID %d not found", id));
    }

    public VehicleNotFoundException(String message) {
        super(message);
    }
}

