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

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI coreFinanceOpenAPI() {
        return new OpenAPI()
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
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + normalizedContextPath())
                                .description("Development Server"),
                        new Server()
                                .url("https://finance-api.company.com" + normalizedContextPath())
                                .description("Production Server")))
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

    private String normalizedContextPath() {
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath.trim())) {
            return "";
        }
        return contextPath.startsWith("/") ? contextPath : "/" + contextPath;
    }
}