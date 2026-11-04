package com.sojibulislamrana.vms_backend.controller;

import com.sojibulislamrana.vms_backend.model.Vehicle;
import com.sojibulislamrana.vms_backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("VehicleController Integration Tests")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
    }

    private String createJsonPayload(String vehicleNumber, String company, String type, String brand, String model, String status) {
        return String.format("""
            {
              "vehicleNumber": "%s",
              "company": "%s",
              "type": "%s",
              "brand": "%s",
              "model": "%s",
              "status": "%s"
            }
            """, vehicleNumber, company, type, brand, model, status);
    }

    @Nested
    @DisplayName("POST /api/v1/vehicles (Create)")
    class CreateVehicleEndpoints {

        @Test
        @DisplayName("HTTP 201 Created: Valid payload creates vehicle and returns Location header")
        void createVehicle_success() throws Exception {
            String payload = createJsonPayload("STK-1001", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active");

            mockMvc.perform(post("/api/v1/vehicles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/vehicles/")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.vehicleNumber").value("STK-1001"))
                    .andExpect(jsonPath("$.company").value("Stark Logistics"))
                    .andExpect(jsonPath("$.type").value("Electric"))
                    .andExpect(jsonPath("$.brand").value("Audi"))
                    .andExpect(jsonPath("$.model").value("e-tron GT"))
                    .andExpect(jsonPath("$.status").value("Active"));

            assertThat(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1001")).isTrue();
        }

        @Test
        @DisplayName("HTTP 400 Bad Request: Missing required fields triggers Bean Validation error response")
        void createVehicle_validationError() throws Exception {
            String invalidPayload = """
                {
                  "vehicleNumber": "",
                  "company": ""
                }
                """;

            mockMvc.perform(post("/api/v1/vehicles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidPayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.validationErrors.vehicleNumber").isNotEmpty())
                    .andExpect(jsonPath("$.validationErrors.company").isNotEmpty());
        }

        @Test
        @DisplayName("HTTP 409 Conflict: Duplicate vehicleNumber returns structured conflict error response")
        void createVehicle_duplicateVehicleNumber_returnsConflict() throws Exception {
            vehicleRepository.save(new Vehicle("STK-1001", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active"));

            String duplicatePayload = createJsonPayload("STK-1001", "Stark Security", "SUV", "Tesla", "Model X", "Active");

            mockMvc.perform(post("/api/v1/vehicles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(duplicatePayload))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value(containsString("STK-1001")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vehicles (List & Read)")
    class ReadVehicleEndpoints {

        @Test
        @DisplayName("HTTP 200 OK: Retrieves all vehicles")
        void getAllVehicles_returnsList() throws Exception {
            vehicleRepository.save(new Vehicle("STK-1001", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active"));
            vehicleRepository.save(new Vehicle("STK-1002", "Stark Security", "SUV", "Tesla", "Model X", "Active"));

            mockMvc.perform(get("/api/v1/vehicles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].vehicleNumber").value("STK-1001"))
                    .andExpect(jsonPath("$[1].vehicleNumber").value("STK-1002"));
        }

        @Test
        @DisplayName("HTTP 200 OK: Retrieves single vehicle by ID")
        void getVehicleById_success() throws Exception {
            Vehicle saved = vehicleRepository.save(new Vehicle("STK-1001", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active"));

            mockMvc.perform(get("/api/v1/vehicles/{id}", saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId()))
                    .andExpect(jsonPath("$.vehicleNumber").value("STK-1001"))
                    .andExpect(jsonPath("$.company").value("Stark Logistics"));
        }

        @Test
        @DisplayName("HTTP 404 Not Found: Requesting non-existent vehicle ID returns error response")
        void getVehicleById_notFound() throws Exception {
            mockMvc.perform(get("/api/v1/vehicles/{id}", 99999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("99999")));
        }

        @Test
        @DisplayName("HTTP 200 OK: Retrieves single vehicle by vehicle number")
        void getVehicleByNumber_success() throws Exception {
            vehicleRepository.save(new Vehicle("STK-3003", "Stark Fleet", "Truck", "Ford", "F-150", "Active"));

            mockMvc.perform(get("/api/v1/vehicles/number/{vehicleNumber}", "STK-3003"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vehicleNumber").value("STK-3003"))
                    .andExpect(jsonPath("$.brand").value("Ford"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/vehicles/{id} (Update)")
    class UpdateVehicleEndpoints {

        @Test
        @DisplayName("HTTP 200 OK: Successfully updates an existing vehicle")
        void updateVehicle_success() throws Exception {
            Vehicle saved = vehicleRepository.save(new Vehicle("STK-1001", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active"));

            String updatePayload = createJsonPayload("STK-1001", "Stark Security", "Electric", "Audi", "e-tron GT", "Maintenance");

            mockMvc.perform(put("/api/v1/vehicles/{id}", saved.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatePayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId()))
                    .andExpect(jsonPath("$.company").value("Stark Security"))
                    .andExpect(jsonPath("$.status").value("Maintenance"));
        }

        @Test
        @DisplayName("HTTP 409 Conflict: Updating to another vehicle's vehicleNumber returns conflict")
        void updateVehicle_duplicateVehicleNumber_returnsConflict() throws Exception {
            Vehicle v1 = vehicleRepository.save(new Vehicle("STK-1001", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active"));
            Vehicle v2 = vehicleRepository.save(new Vehicle("STK-1002", "Stark Security", "SUV", "Tesla", "Model X", "Active"));

            // Attempt to update v2's vehicle number to v1's number
            String duplicatePayload = createJsonPayload("STK-1001", "Stark Security", "SUV", "Tesla", "Model X", "Active");

            mockMvc.perform(put("/api/v1/vehicles/{id}", v2.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(duplicatePayload))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value(containsString("STK-1001")));
        }

        @Test
        @DisplayName("HTTP 404 Not Found: Updating non-existent vehicle returns not found")
        void updateVehicle_notFound() throws Exception {
            String payload = createJsonPayload("STK-1001", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active");

            mockMvc.perform(put("/api/v1/vehicles/{id}", 99999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/vehicles/{id} (Delete)")
    class DeleteVehicleEndpoints {

        @Test
        @DisplayName("HTTP 204 No Content: Successfully deletes vehicle")
        void deleteVehicle_success() throws Exception {
            Vehicle saved = vehicleRepository.save(new Vehicle("STK-1001", "Stark Logistics", "Electric", "Audi", "e-tron GT", "Active"));

            mockMvc.perform(delete("/api/v1/vehicles/{id}", saved.getId()))
                    .andExpect(status().isNoContent());

            assertThat(vehicleRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("HTTP 404 Not Found: Deleting non-existent vehicle returns not found")
        void deleteVehicle_notFound() throws Exception {
            mockMvc.perform(delete("/api/v1/vehicles/{id}", 99999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}

