package com.sojibulislamrana.vms_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an operation attempts to register or update a vehicle
 * with a vehicleNumber that already exists in the system.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateVehicleNumberException extends RuntimeException {

    private final String vehicleNumber;

    public DuplicateVehicleNumberException(String vehicleNumber) {
        super(String.format("Vehicle with number '%s' already exists", vehicleNumber));
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }
}

