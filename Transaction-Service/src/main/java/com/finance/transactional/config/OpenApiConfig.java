package com.finance.transactional.config;

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

@Configuration
public class OpenApiConfig {

    @Value("${KEYCLOAK_ISSUER_URI}")
    private String keycloakOpenIdConnectUrl;

    @Value("${api-gateway-base-url}")
    private String apiGatewayBaseUrl;

    @Value("${server.port}")
    private String serverPort;
    
    @Value("${TRANSACTION_SERVICE_PORT:8083}")
    private String externalPort;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        String localhostUrl = "http://localhost:" + this.externalPort;
        String applicationName = this.capitalizeApplicationName(this.applicationName);

        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " APIs")
                        .version("1.0")
                        .description("SaaS ERP " + applicationName + " APIs")
                        .termsOfService("Terms of service")
                        .contact(new Contact()
                                .name("Owner name")
                                .email("Owner email address")
                                .url("Owner website url"))
                        .license(new License().name("License name").url("License url")))
                .addServersItem(new Server().url(apiGatewayBaseUrl).description("Remote ENV"))
                .addServersItem(new Server().url(localhostUrl).description("Local ENV"))
                .addSecurityItem(new SecurityRequirement().addList("Keycloak"))
                .schemaRequirement(
                        "Keycloak",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER));
    }

    private String capitalizeApplicationName(String applicationName) {
        String[] words = applicationName.split("-");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized
                        .append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return capitalized.toString().trim();
    }
}
