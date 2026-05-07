package com.finance.transactional.config.security;

import com.finance.transactional.config.RoleConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RoleConverter roleConverter;

    @Value("${transactional.security.enabled:false}")
    private boolean securityEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);

        if (securityEnabled) {
            http.authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/swagger-ui/**", "/swagger", "/v3/api-docs/**", "/actuator/**")
                    .permitAll()
                    .anyRequest().authenticated());
            http.oauth2ResourceServer((oauth2 ->
                    oauth2.jwt(customizer -> customizer.jwtAuthenticationConverter(roleConverter))));
        } else {
            http.authorizeHttpRequests(authorize -> authorize
                    .anyRequest().permitAll());
        }

        http.sessionManagement(t ->
                t.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
