package com.example.cost_service.config;

import com.example.cost_service.utility.SecurityUtil;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final SecurityUtil securityUtil;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String token = securityUtil.getUserToken();
            if (token != null) {
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}
