package com.sojibulislamrana.vms_backend.repository;

import com.sojibulislamrana.vms_backend.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence layer for Vehicle domain entities.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Check if a vehicle already exists with the given vehicle number (case-insensitive).
     */
    boolean existsByVehicleNumberIgnoreCase(String vehicleNumber);

    /**
     * Find a vehicle by vehicle number (case-insensitive).
     */
    Optional<Vehicle> findByVehicleNumberIgnoreCase(String vehicleNumber);

    /**
     * Find all vehicles assigned to a specific company (case-insensitive).
     */
    List<Vehicle> findByCompanyIgnoreCase(String company);

    /**
     * Find all vehicles with a specific status (case-insensitive).
     */
    List<Vehicle> findByStatusIgnoreCase(String status);
}

