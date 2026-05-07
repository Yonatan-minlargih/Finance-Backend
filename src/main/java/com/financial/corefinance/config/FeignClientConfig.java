package com.financial.corefinance.config;

import feign.RequestInterceptor;
import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignRequestInterceptor();
    }

    @Bean
    public Retryer retryer() {
        return new CustomRetryer();
    }

    public static class FeignRequestInterceptor implements RequestInterceptor {
        @Override
        public void apply(feign.RequestTemplate template) {
            // Add common headers
            template.header("X-Service-Name", "core-finance-service");
            template.header("X-Request-Id", java.util.UUID.randomUUID().toString());
            template.header("X-Timestamp", String.valueOf(System.currentTimeMillis()));
        }
    }

    public static class CustomRetryer implements Retryer {
        private final int maxAttempts = 3;
        private final long retryInterval = 1000; // 1 second
        private int attempt = 1;

        @Override
        public void continueOrPropagate(RetryableException e) {
            if (attempt++ >= maxAttempts) {
                log.error("Max retry attempts reached for Feign client call", e);
                throw e;
            }
            
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted during retry", ie);
            }
        }

        @Override
        public Retryer clone() {
            return new CustomRetryer();
        }
    }
}
