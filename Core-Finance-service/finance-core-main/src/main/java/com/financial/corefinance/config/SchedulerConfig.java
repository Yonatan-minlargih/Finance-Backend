package com.financial.corefinance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableScheduling
@EnableAsync
@Slf4j
public class SchedulerConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Finance-Async-");
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("Task {} was rejected by thread pool {}", r, executor1);
            // Custom rejection handling
        });
        executor.initialize();
        return executor;
    }

    @Bean(name = "scheduledTaskExecutor")
    public Executor scheduledTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Finance-Scheduled-");
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("Scheduled task {} was rejected by thread pool {}", r, executor1);
            // Custom rejection handling
        });
        executor.initialize();
        return executor;
    }
}
