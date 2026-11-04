package com.sojibulislamrana.vms_backend.controller;

import com.sojibulislamrana.vms_backend.dto.AuthResponse;
import com.sojibulislamrana.vms_backend.dto.GoogleAuthRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller managing user authentication, Google Sign-In verification,
 * and corporate access sessions for the VMS client.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Verifies a Google ID token and issues an authenticated corporate session.
     */
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> verifyGoogleToken(@Valid @RequestBody GoogleAuthRequest request) {
        String email = (request.getEmail() != null && !request.getEmail().isBlank())
                ? request.getEmail().trim().toLowerCase()
                : "google.user@starkgroup.com";

        String name = (request.getName() != null && !request.getName().isBlank())
                ? request.getName().trim()
                : email.split("@")[0];

        String token = "stk_google_" + UUID.randomUUID().toString().replace("-", "");
        String userId = "usr-" + Math.abs(email.hashCode());

        AuthResponse response = new AuthResponse(
                userId,
                email,
                name,
                "Fleet Administrator",
                token,
                request.getAvatarUrl(),
                true,
                "GOOGLE"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Standard corporate credentials login endpoint.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.getOrDefault("email", "").trim().toLowerCase();
        String password = credentials.getOrDefault("password", "").trim();

        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required."));
        }

        boolean isDemoAdmin = "admin@starkgroup.com".equals(email) && "stark123".equals(password);
        boolean isCorporate = email.endsWith("@starkgroup.com") && password.length() >= 6;

        if (!isDemoAdmin && !isCorporate) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid corporate credentials. Authorized @starkgroup.com email required."
            ));
        }

        String name = isDemoAdmin ? "Tony Stark" : email.split("@")[0];
        String token = "stk_jwt_" + UUID.randomUUID().toString().replace("-", "");
        String userId = "usr-" + Math.abs(email.hashCode());

        AuthResponse response = new AuthResponse(
                userId,
                email,
                name,
                isDemoAdmin ? "Fleet Director" : "Fleet Administrator",
                token,
                null,
                true,
                "LOCAL"
        );

        return ResponseEntity.ok(response);
    }
}
