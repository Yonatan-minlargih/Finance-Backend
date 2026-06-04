package com.financial.corefinance.config;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "integration.gl-accounts")
public class IntegrationGlAccountProperties {

    private boolean enabled = true;
    private boolean seedOnStartup = true;
    private String baseCurrency = "ETB";
    private String seedTenants = "";
    private Map<String, AccountDefinition> accounts = new LinkedHashMap<>();

    @Data
    public static class AccountDefinition {
        private String code;
        private String name;
        private String accountType;
        private String normalBalance;
        private String ifrsCategory;
        private String module;
        private String description;
        private Boolean allowManualEntry = true;
    }
}
