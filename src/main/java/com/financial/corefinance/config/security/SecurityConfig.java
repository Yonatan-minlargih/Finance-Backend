package com.financial.corefinance.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**","/swagger-ui.html","/v3/api-docs.yaml","/v3/api-docs").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                
                // Protected endpoints
                .requestMatchers("/api/v1/journals/**").hasAnyRole("ACCOUNTANT", "FINANCE_MANAGER", "AUDITOR")
                .requestMatchers("/api/v1/accounts/**").hasAnyRole("ACCOUNTANT", "FINANCE_MANAGER", "AUDITOR")
                .requestMatchers("/api/v1/budgets/**").hasAnyRole("FINANCE_MANAGER", "BUDGET_MANAGER", "AUDITOR")
                .requestMatchers("/api/v1/periods/**").hasAnyRole("ACCOUNTANT", "FINANCE_MANAGER", "AUDITOR")
                .requestMatchers("/api/v1/ifrs-reports/**").hasAnyRole("FINANCE_MANAGER", "REPORT_MANAGER", "AUDITOR")
                
                // Admin endpoints
                .requestMatchers("/api/v1/admin/**").hasRole("SYSTEM_ADMIN")
                
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
