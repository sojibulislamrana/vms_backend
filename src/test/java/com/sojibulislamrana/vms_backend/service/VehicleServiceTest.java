package com.sojibulislamrana.vms_backend.service;

import com.sojibulislamrana.vms_backend.dto.VehicleRequest;
import com.sojibulislamrana.vms_backend.dto.VehicleResponse;
import com.sojibulislamrana.vms_backend.exception.DuplicateVehicleNumberException;
import com.sojibulislamrana.vms_backend.exception.VehicleNotFoundException;
import com.sojibulislamrana.vms_backend.model.Vehicle;
import com.sojibulislamrana.vms_backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Business Rules Unit Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private VehicleRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new VehicleRequest(
                "STK-1001",
                "Stark Logistics",
                "Electric",
                "Audi",
                "e-tron GT",
                "Active"
        );
    }

    @Nested
    @DisplayName("Create Vehicle Rules")
    class CreateVehicleRules {

        @Test
        @DisplayName("Rule: Duplicate Vehicle Number throws DuplicateVehicleNumberException on creation")
        void createVehicle_whenVehicleNumberAlreadyExists_throwsDuplicateVehicleNumberException() {
            // Arrange
            when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1001")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> vehicleService.createVehicle(sampleRequest))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessageContaining("STK-1001")
                    .satisfies(ex -> {
                        DuplicateVehicleNumberException dupEx = (DuplicateVehicleNumberException) ex;
                        assertThat(dupEx.getVehicleNumber()).isEqualTo("STK-1001");
                    });

            // Ensure no persistence occurs when business rule is violated
            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Rule: Case-insensitive duplicate Vehicle Number check on creation")
        void createVehicle_withDifferentCasing_isStillDetectedAsDuplicate() {
            // Arrange
            sampleRequest.setVehicleNumber("stk-1001");
            when(vehicleRepository.existsByVehicleNumberIgnoreCase("stk-1001")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> vehicleService.createVehicle(sampleRequest))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessageContaining("stk-1001");

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Rule: Unique Vehicle Number allows creation and persists entity")
        void createVehicle_whenVehicleNumberIsUnique_persistsAndReturnsResponse() {
            // Arrange
            when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1001")).thenReturn(false);

            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
                Vehicle v = invocation.getArgument(0);
                v.setId(1L);
                return v;
            });

            // Act
            VehicleResponse response = vehicleService.createVehicle(sampleRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getVehicleNumber()).isEqualTo("STK-1001");
            assertThat(response.getCompany()).isEqualTo("Stark Logistics");
            assertThat(response.getBrand()).isEqualTo("Audi");
            assertThat(response.getModel()).isEqualTo("e-tron GT");
            assertThat(response.getType()).isEqualTo("Electric");
            assertThat(response.getStatus()).isEqualTo("Active");

            ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepository).save(vehicleCaptor.capture());
            assertThat(vehicleCaptor.getValue().getVehicleNumber()).isEqualTo("STK-1001");
        }
    }

    @Nested
    @DisplayName("Update Vehicle Rules")
    class UpdateVehicleRules {

        @Test
        @DisplayName("Rule: Updating to an existing Vehicle Number throws DuplicateVehicleNumberException")
        void updateVehicle_whenChangingToExistingVehicleNumber_throwsDuplicateVehicleNumberException() {
            // Arrange
            Vehicle existingVehicle = new Vehicle(
                    "STK-1001",
                    "Stark Logistics",
                    "Electric",
                    "Audi",
                    "e-tron GT",
                    "Active"
            );
            existingVehicle.setId(1L);

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));

            VehicleRequest updateRequest = new VehicleRequest(
                    "STK-1002", // Changed number that already exists
                    "Stark Logistics",
                    "Electric",
                    "Audi",
                    "e-tron GT",
                    "Active"
            );
            when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1002")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> vehicleService.updateVehicle(1L, updateRequest))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessageContaining("STK-1002");

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Rule: Keeping the same Vehicle Number during update does NOT trigger duplicate error")
        void updateVehicle_whenVehicleNumberIsUnchanged_updatesSuccessfully() {
            // Arrange
            Vehicle existingVehicle = new Vehicle(
                    "STK-1001",
                    "Stark Logistics",
                    "Electric",
                    "Audi",
                    "e-tron GT",
                    "Active"
            );
            existingVehicle.setId(1L);

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));

            VehicleRequest updateRequest = new VehicleRequest(
                    "STK-1001", // Identical number
                    "Stark Security", // Updated company
                    "Electric",
                    "Audi",
                    "e-tron GT",
                    "Maintenance" // Updated status
            );

            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            VehicleResponse response = vehicleService.updateVehicle(1L, updateRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getVehicleNumber()).isEqualTo("STK-1001");
            assertThat(response.getCompany()).isEqualTo("Stark Security");
            assertThat(response.getStatus()).isEqualTo("Maintenance");

            // Should NOT query for duplicate check because number didn't change
            verify(vehicleRepository, never()).existsByVehicleNumberIgnoreCase(anyString());
            verify(vehicleRepository).save(existingVehicle);
        }

        @Test
        @DisplayName("Rule: Changing to a new unique Vehicle Number updates successfully")
        void updateVehicle_whenChangingToUniqueVehicleNumber_updatesSuccessfully() {
            // Arrange
            Vehicle existingVehicle = new Vehicle(
                    "STK-1001",
                    "Stark Logistics",
                    "Electric",
                    "Audi",
                    "e-tron GT",
                    "Active"
            );
            existingVehicle.setId(1L);

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));

            VehicleRequest updateRequest = new VehicleRequest(
                    "STK-9999", // New unique number
                    "Stark Fleet",
                    "Electric",
                    "Audi",
                    "e-tron GT",
                    "Active"
            );
            when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-9999")).thenReturn(false);
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            VehicleResponse response = vehicleService.updateVehicle(1L, updateRequest);

            // Assert
            assertThat(response.getVehicleNumber()).isEqualTo("STK-9999");
            assertThat(response.getCompany()).isEqualTo("Stark Fleet");
            verify(vehicleRepository).existsByVehicleNumberIgnoreCase("STK-9999");
            verify(vehicleRepository).save(existingVehicle);
        }

        @Test
        @DisplayName("Rule: Updating non-existent vehicle throws VehicleNotFoundException")
        void updateVehicle_whenNotFound_throwsVehicleNotFoundException() {
            when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.updateVehicle(999L, sampleRequest))
                    .isInstanceOf(VehicleNotFoundException.class)
                    .hasMessageContaining("999");

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }
    }
}

