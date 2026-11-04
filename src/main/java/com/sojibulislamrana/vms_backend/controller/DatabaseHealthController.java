package com.sojibulislamrana.vms_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller providing runtime verification of database connectivity
 * without requiring any specific domain tables or entities.
 */
@RestController
@RequestMapping("/api/health")
public class DatabaseHealthController {

    private final DataSource dataSource;

    public DatabaseHealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkDatabaseHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("service", "vms_backend");

        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2);
            DatabaseMetaData metaData = connection.getMetaData();

            response.put("status", isValid ? "UP" : "DOWN");
            response.put("databaseConnected", isValid);
            response.put("databaseProduct", metaData.getDatabaseProductName());
            response.put("databaseVersion", metaData.getDatabaseProductVersion());
            response.put("driverName", metaData.getDriverName());

            return isValid
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.status(503).body(response);
        } catch (Exception ex) {
            response.put("status", "DOWN");
            response.put("databaseConnected", false);
            response.put("error", ex.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }
}

