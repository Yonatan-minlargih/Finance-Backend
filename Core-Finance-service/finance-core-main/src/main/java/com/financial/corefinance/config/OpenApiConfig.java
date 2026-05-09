package com.financial.corefinance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${swagger.base-url:}")
    private String swaggerBaseUrl;

    @Bean
    public OpenAPI coreFinanceOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Core Finance Service API")
                        .description("Central General Ledger Service for Financial Management System. This service provides comprehensive financial accounting capabilities including journal posting, chart of accounts management, budgeting, and IFRS reporting.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Financial Systems Team")
                                .email("finance-team@company.com")
                                .url("https://finance.company.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));

        List<Server> servers = new ArrayList<>();
        
        // If a base URL is provided via environment variable, use it. 
        // Otherwise, it will default to the current host in the browser.
        if (!swaggerBaseUrl.isBlank()) {
            servers.add(new Server().url(swaggerBaseUrl).description("Configured Server"));
        } else {
            servers.add(new Server().url("/").description("Default Server (Relative)"));
        }
        
        return openAPI
                .servers(servers)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token. Include 'X-Tenant-ID' header for multi-tenancy."))
                        .addSecuritySchemes("tenantAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Tenant-ID")
                                .description("Tenant identifier for multi-tenancy support")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement().addList("tenantAuth"));
    }
}