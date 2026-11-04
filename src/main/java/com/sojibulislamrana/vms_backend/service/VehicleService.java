package com.sojibulislamrana.vms_backend.service;

import com.sojibulislamrana.vms_backend.dto.VehicleRequest;
import com.sojibulislamrana.vms_backend.dto.VehicleResponse;

import java.util.List;

/**
 * Service interface encapsulating business rules and workflows for Vehicle management.
 */
public interface VehicleService {

    /**
     * Register a new vehicle.
     * Enforces business rule: Vehicle Number must be unique across the fleet.
     *
     * @param request valid vehicle creation payload
     * @return created vehicle representation
     * @throws com.sojibulislamrana.vms_backend.exception.DuplicateVehicleNumberException if vehicleNumber already exists
     */
    VehicleResponse createVehicle(VehicleRequest request);

    /**
     * Update an existing vehicle record.
     * Enforces business rule: If vehicleNumber is modified, the new number must be unique.
     *
     * @param id vehicle primary key identifier
     * @param request valid vehicle update payload
     * @return updated vehicle representation
     * @throws com.sojibulislamrana.vms_backend.exception.VehicleNotFoundException if vehicle with given ID does not exist
     * @throws com.sojibulislamrana.vms_backend.exception.DuplicateVehicleNumberException if updated vehicleNumber belongs to another vehicle
     */
    VehicleResponse updateVehicle(Long id, VehicleRequest request);

    /**
     * Retrieve a vehicle by its generated ID.
     */
    VehicleResponse getVehicleById(Long id);

    /**
     * Retrieve a vehicle by its unique vehicle number.
     */
    VehicleResponse getVehicleByNumber(String vehicleNumber);

    /**
     * Retrieve all fleet vehicles.
     */
    List<VehicleResponse> getAllVehicles();

    /**
     * Remove a vehicle by its ID.
     */
    void deleteVehicle(Long id);
}

