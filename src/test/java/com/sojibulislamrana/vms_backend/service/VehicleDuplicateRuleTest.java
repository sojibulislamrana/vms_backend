package com.sojibulislamrana.vms_backend.service;

import com.sojibulislamrana.vms_backend.dto.VehicleRequest;
import com.sojibulislamrana.vms_backend.dto.VehicleResponse;
import com.sojibulislamrana.vms_backend.exception.DuplicateVehicleNumberException;
import com.sojibulislamrana.vms_backend.model.Vehicle;
import com.sojibulislamrana.vms_backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

/**
 * Focused unit test verifying the duplicate Vehicle Number business rule.
 * The VehicleRepository is mocked to isolate business logic from database persistence.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Vehicle Service - Duplicate Vehicle Number Rule Tests")
class VehicleDuplicateRuleTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private VehicleRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new VehicleRequest(
                "STK-1001",
                "Stark Logistics",
                "Electric",
                "Audi",
                "e-tron GT",
                "Active"
        );
    }

    @Test
    @DisplayName("Given an existing Vehicle Number, when registering, then reject with DuplicateVehicleNumberException and do not persist")
    void givenExistingVehicleNumber_whenCreateVehicle_thenThrowsDuplicateVehicleNumberException() {
        // Given: Repository reports that vehicle number 'STK-1001' already exists
        when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1001")).thenReturn(true);

        // When & Then: Attempting to create the vehicle must throw DuplicateVehicleNumberException
        assertThatThrownBy(() -> vehicleService.createVehicle(validRequest))
                .isInstanceOf(DuplicateVehicleNumberException.class)
                .hasMessage("Vehicle with number 'STK-1001' already exists")
                .satisfies(exception -> {
                    DuplicateVehicleNumberException dupEx = (DuplicateVehicleNumberException) exception;
                    assertThat(dupEx.getVehicleNumber()).isEqualTo("STK-1001");
                });

        // Ensure no entity was saved to the repository
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Given a new unique Vehicle Number, when registering, then accept and persist the vehicle")
    void givenNewVehicleNumber_whenCreateVehicle_thenAcceptsAndPersistsVehicle() {
        // Given: Repository reports that vehicle number 'STK-1001' is not taken
        when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1001")).thenReturn(false);

        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicleToSave = invocation.getArgument(0);
            vehicleToSave.setId(42L);
            return vehicleToSave;
        });

        // When: Creating the vehicle
        VehicleResponse response = vehicleService.createVehicle(validRequest);

        // Then: Creation succeeds with expected values
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getVehicleNumber()).isEqualTo("STK-1001");
        assertThat(response.getCompany()).isEqualTo("Stark Logistics");
        assertThat(response.getStatus()).isEqualTo("Active");

        // Verify save was invoked exactly once with normalized vehicle number
        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository, times(1)).save(vehicleCaptor.capture());
        assertThat(vehicleCaptor.getValue().getVehicleNumber()).isEqualTo("STK-1001");
    }

    @Test
    @DisplayName("Given existing Vehicle Number in different casing, when registering, then reject case-insensitively")
    void givenExistingVehicleNumberInDifferentCasing_whenCreateVehicle_thenRejectsDuplicate() {
        // Given: Request uses lowercase 'stk-1001' while existing record is uppercase
        validRequest.setVehicleNumber("stk-1001");
        when(vehicleRepository.existsByVehicleNumberIgnoreCase("stk-1001")).thenReturn(true);

        // When & Then: Duplicate is still rejected
        assertThatThrownBy(() -> vehicleService.createVehicle(validRequest))
                .isInstanceOf(DuplicateVehicleNumberException.class)
                .hasMessageContaining("stk-1001");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Given existing vehicle, when updating to another vehicle's number that already exists, then reject duplicate")
    void givenExistingVehicle_whenUpdatingToConflictingNumber_thenThrowsDuplicateVehicleNumberException() {
        // Given: Existing vehicle has number STK-1001
        Vehicle existingVehicle = new Vehicle("STK-1001", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active");
        existingVehicle.setId(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));

        // When updating to STK-1002 which already belongs to another vehicle
        VehicleRequest updateRequest = new VehicleRequest("STK-1002", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active");
        when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1002")).thenReturn(true);

        // Then: Update is rejected
        assertThatThrownBy(() -> vehicleService.updateVehicle(1L, updateRequest))
                .isInstanceOf(DuplicateVehicleNumberException.class)
                .hasMessage("Vehicle with number 'STK-1002' already exists");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Given existing vehicle, when updating non-identifying fields with the same Vehicle Number, then accept without duplicate error")
    void givenExistingVehicle_whenUpdatingWithoutChangingVehicleNumber_thenAcceptsUpdate() {
        // Given: Existing vehicle
        Vehicle existingVehicle = new Vehicle("STK-1001", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active");
        existingVehicle.setId(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));

        // When updating company and status but retaining the same vehicle number
        VehicleRequest updateRequest = new VehicleRequest("STK-1001", "Stark Security", "Electric", "Audi", "e-tron GT", "Maintenance");
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponse response = vehicleService.updateVehicle(1L, updateRequest);

        // Then: Update succeeds and duplicate check query is skipped
        assertThat(response).isNotNull();
        assertThat(response.getVehicleNumber()).isEqualTo("STK-1001");
        assertThat(response.getCompany()).isEqualTo("Stark Security");
        assertThat(response.getStatus()).isEqualTo("Maintenance");

        verify(vehicleRepository, never()).existsByVehicleNumberIgnoreCase(anyString());
        verify(vehicleRepository, times(1)).save(existingVehicle);
    }
}

