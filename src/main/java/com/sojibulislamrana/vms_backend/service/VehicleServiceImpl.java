package com.sojibulislamrana.vms_backend.service;

import com.sojibulislamrana.vms_backend.dto.VehicleRequest;
import com.sojibulislamrana.vms_backend.dto.VehicleResponse;
import com.sojibulislamrana.vms_backend.exception.DuplicateVehicleNumberException;
import com.sojibulislamrana.vms_backend.exception.VehicleNotFoundException;
import com.sojibulislamrana.vms_backend.model.Vehicle;
import com.sojibulislamrana.vms_backend.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation containing core business rules for the Vehicle domain.
 */
@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public VehicleResponse createVehicle(VehicleRequest request) {
        final String normalizedNumber = request.getVehicleNumber().trim();

        // Business rule: Vehicle Number must be unique across the fleet
        if (vehicleRepository.existsByVehicleNumberIgnoreCase(normalizedNumber)) {
            throw new DuplicateVehicleNumberException(normalizedNumber);
        }

        Vehicle vehicle = new Vehicle(
                normalizedNumber,
                request.getCompany().trim(),
                request.getType().trim(),
                request.getBrand().trim(),
                request.getModel().trim(),
                request.getStatus().trim()
        );

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(saved);
    }

    @Override
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        final String normalizedNumber = request.getVehicleNumber().trim();

        // Business rule: If vehicleNumber is modified, the new number must be unique
        if (!existing.getVehicleNumber().equalsIgnoreCase(normalizedNumber)) {
            if (vehicleRepository.existsByVehicleNumberIgnoreCase(normalizedNumber)) {
                throw new DuplicateVehicleNumberException(normalizedNumber);
            }
            existing.setVehicleNumber(normalizedNumber);
        }

        existing.setCompany(request.getCompany().trim());
        existing.setType(request.getType().trim());
        existing.setBrand(request.getBrand().trim());
        existing.setModel(request.getModel().trim());
        existing.setStatus(request.getStatus().trim());

        Vehicle updated = vehicleRepository.save(existing);
        return VehicleResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .map(VehicleResponse::fromEntity)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleByNumber(String vehicleNumber) {
        final String normalizedNumber = vehicleNumber.trim();
        return vehicleRepository.findByVehicleNumberIgnoreCase(normalizedNumber)
                .map(VehicleResponse::fromEntity)
                .orElseThrow(() -> new VehicleNotFoundException(
                        String.format("Vehicle with number '%s' not found", normalizedNumber)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(VehicleResponse::fromEntity)
                .toList();
    }

    @Override
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new VehicleNotFoundException(id);
        }
        vehicleRepository.deleteById(id);
    }
}

