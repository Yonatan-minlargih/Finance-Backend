package com.example.payroll_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // IMPORTANT: In a production-level project, never use "*" for origins when credentials are allowed.
        // Explicitly list the URLs where your frontend will be hosted.
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173", // Vite Dev Server
                "http://localhost:3000"  // Potential alternative React dev server
                // "https://finance.yourdomain.com" // Add your production domain here
        ));

        // Allow the frontend to send cookies and authorization headers
        config.setAllowCredentials(true);

        // Explicitly allow the headers your frontend sends
        config.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Tenant-ID"
        ));

        // Explicitly allow the methods, crucially including OPTIONS for CORS preflight
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "OPTIONS",
                "DELETE",
                "PATCH"
        ));

        // Cache the preflight response for 1 hour to reduce network traffic
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this configuration to all endpoints
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
