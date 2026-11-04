package com.sojibulislamrana.vms_backend.repository;

import com.sojibulislamrana.vms_backend.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("VehicleRepository Persistence Integration Tests")
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
    }

    @Test
    @DisplayName("Persist vehicle entity and verify generated primary key")
    void testSaveAndFindById() {
        Vehicle vehicle = new Vehicle(
                "STK-1001",
                "Stark Logistics",
                "Electric",
                "Audi",
                "e-tron GT",
                "Active"
        );

        Vehicle saved = vehicleRepository.save(vehicle);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<Vehicle> retrieved = vehicleRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getVehicleNumber()).isEqualTo("STK-1001");
        assertThat(retrieved.get().getCompany()).isEqualTo("Stark Logistics");
    }

    @Test
    @DisplayName("Verify existsByVehicleNumberIgnoreCase query method")
    void testExistsByVehicleNumberIgnoreCase() {
        Vehicle vehicle = new Vehicle(
                "STK-2002",
                "Stark Security",
                "SUV",
                "Tesla",
                "Model X Plaid",
                "Active"
        );
        vehicleRepository.save(vehicle);

        assertThat(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-2002")).isTrue();
        assertThat(vehicleRepository.existsByVehicleNumberIgnoreCase("stk-2002")).isTrue();
        assertThat(vehicleRepository.existsByVehicleNumberIgnoreCase("NON_EXISTENT")).isFalse();
    }

    @Test
    @DisplayName("Find vehicles by company case-insensitively")
    void testFindByCompanyIgnoreCase() {
        vehicleRepository.save(new Vehicle("STK-3001", "Stark Logistics", "Truck", "Ford", "F-150", "Active"));
        vehicleRepository.save(new Vehicle("STK-3002", "stark logistics", "Van", "Mercedes", "Sprinter", "Maintenance"));
        vehicleRepository.save(new Vehicle("STK-3003", "Stark Security", "Sedan", "BMW", "i7", "Active"));

        List<Vehicle> logisticsFleet = vehicleRepository.findByCompanyIgnoreCase("STARK LOGISTICS");
        assertThat(logisticsFleet).hasSize(2);
    }
}

