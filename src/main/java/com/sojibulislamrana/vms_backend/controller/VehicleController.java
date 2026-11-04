package com.sojibulislamrana.vms_backend.controller;

import com.sojibulislamrana.vms_backend.dto.VehicleRequest;
import com.sojibulislamrana.vms_backend.dto.VehicleResponse;
import com.sojibulislamrana.vms_backend.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller exposing endpoints for managing fleet vehicles.
 * Strictly uses request/response DTOs to avoid exposing JPA entities.
 */
@RestController
@RequestMapping({"/api/v1/vehicles", "/api/vehicles"})
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * Create a new vehicle.
     * HTTP 201 Created with Location header and VehicleResponse body.
     */
    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse created = vehicleService.createVehicle(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/vehicles/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * List all fleet vehicles.
     * HTTP 200 OK with list of VehicleResponse.
     */
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    /**
     * Retrieve a single vehicle by its primary key ID.
     * HTTP 200 OK or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    /**
     * Retrieve a single vehicle by its unique vehicle number.
     * HTTP 200 OK or 404 Not Found.
     */
    @GetMapping("/number/{vehicleNumber}")
    public ResponseEntity<VehicleResponse> getVehicleByNumber(@PathVariable String vehicleNumber) {
        return ResponseEntity.ok(vehicleService.getVehicleByNumber(vehicleNumber));
    }

    /**
     * Update an existing vehicle record.
     * HTTP 200 OK, 404 Not Found, or 409 Conflict.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, request));
    }

    /**
     * Delete a vehicle by its ID.
     * HTTP 204 No Content or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}

