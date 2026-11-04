package com.sojibulislamrana.vms_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Production-ready CORS configuration for the VMS REST API.
 * Configures allowed origins dynamically via the CORS_ALLOWED_ORIGINS environment variable,
 * supporting both local development and deployed Flutter Web production domains.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:*,http://127.0.0.1:*,https://*.netlify.app,https://*.onrender.com,https://*.pages.dev,https://*.vercel.app}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Location", "Authorization")
                .allowCredentials(false)
                .maxAge(3600);
    }
}

