package com.financial.corefinance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "budget")
public record BudgetProperties(
        Control control) {

    public BudgetProperties() {
        this(new Control("WARN", 90));
    }

    public record Control(String overspendMode, int warningThresholdPercent) {
        public Control {
            if (overspendMode == null || overspendMode.isBlank()) {
                overspendMode = "WARN";
            }
        }

        public boolean blockOnOverspend() {
            return "BLOCK".equalsIgnoreCase(overspendMode);
        }
    }
}
