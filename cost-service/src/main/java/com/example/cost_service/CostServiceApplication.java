package com.example.cost_service;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@ConfigurationPropertiesScan
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@EnableMethodSecurity
public class CostServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CostServiceApplication.class, args);
	}

}
