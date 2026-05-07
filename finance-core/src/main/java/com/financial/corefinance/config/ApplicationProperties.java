package com.financial.corefinance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
public record ApplicationProperties(
        String financeEventsExchange,
        String createJournalQueue,
        String updateJournalQueue,
        String deleteJournalQueue,
        String createAccountQueue,
        String updateAccountQueue,
        String deleteAccountQueue,
        String createBudgetQueue,
        String updateBudgetQueue,
        String createFiscalYearQueue,
        String closeFiscalYearQueue,
        String generateReportQueue) {}
