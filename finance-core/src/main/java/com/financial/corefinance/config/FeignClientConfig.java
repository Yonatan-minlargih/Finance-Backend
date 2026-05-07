package com.financial.corefinance.config;

import feign.RequestInterceptor;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("X-Service-Name", "core-finance-service");
            template.header("X-Request-Id", UUID.randomUUID().toString());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtToken) {
                template.header("Authorization", "Bearer " + jwtToken.getToken().getTokenValue());
            }
        };
    }
}
