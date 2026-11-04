package com.sojibulislamrana.vms_backend.dto;

import java.time.Instant;

public class AuthResponse {

    private String id;
    private String email;
    private String name;
    private String role;
    private String token;
    private String avatarUrl;
    private boolean verified;
    private String authProvider;
    private String timestamp;

    public AuthResponse() {
        this.timestamp = Instant.now().toString();
    }

    public AuthResponse(String id, String email, String name, String role, String token, String avatarUrl, boolean verified, String authProvider) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.token = token;
        this.avatarUrl = avatarUrl;
        this.verified = verified;
        this.authProvider = authProvider;
        this.timestamp = Instant.now().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

