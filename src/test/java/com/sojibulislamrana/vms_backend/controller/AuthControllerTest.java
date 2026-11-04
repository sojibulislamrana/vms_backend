package com.sojibulislamrana.vms_backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Authentication Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/auth/google verifies token and returns session")
    void verifyGoogleToken_Success() throws Exception {
        String payload = """
            {
              "idToken": "mock-google-id-token-12345",
              "email": "tony@starkgroup.com",
              "name": "Tony Stark",
              "avatarUrl": "https://example.com/avatar.png"
            }
            """;

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.email").value("tony@starkgroup.com"))
                .andExpect(jsonPath("$.name").value("Tony Stark"))
                .andExpect(jsonPath("$.authProvider").value("GOOGLE"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login succeeds with demo credentials")
    void login_DemoSuccess() throws Exception {
        String payload = """
            {
              "email": "admin@starkgroup.com",
              "password": "stark123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.role").value("Fleet Director"));
    }

    @Test
    @DisplayName("POST /api/auth/login rejects invalid credentials")
    void login_InvalidRejected() throws Exception {
        String payload = """
            {
              "email": "hacker@external.com",
              "password": "wrongpass"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }
}
