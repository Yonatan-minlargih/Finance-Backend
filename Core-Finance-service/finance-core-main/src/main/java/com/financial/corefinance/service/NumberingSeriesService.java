package com.financial.corefinance.service;

import com.financial.corefinance.domain.base.TenantContext;
import com.financial.corefinance.domain.entity.NumberingSeries;
import com.financial.corefinance.repository.NumberingSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NumberingSeriesService {

    private final NumberingSeriesRepository repository;

    @Transactional
    public NumberingSeries createSeries(NumberingSeries series) {
        series.setTenantId(TenantContext.getCurrentTenant());
        return repository.save(series);
    }

    @Transactional
    public NumberingSeries updateSeries(UUID id, NumberingSeries updatedFields) {
        return repository.save(updatedFields);
    }

    @Transactional(readOnly = true)
    public NumberingSeries getSeriesById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Numbering series not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<NumberingSeries> getAllSeries() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return repository.findByTenantId(tenantId);
        }
        return repository.findAll();
    }

    @Transactional
    public void deleteSeries(UUID id) {
        NumberingSeries existing = getSeriesById(id);
        repository.delete(existing);
    }

    /**
     * Atomically returns the next document number for a given series code,
     * auto-creating the series if it doesn't exist. Uses pessimistic write lock
     * to prevent race conditions (same pattern as PostingEngineService).
     */
    @Transactional
    public Map<String, String> getNextNumber(String seriesCode) {
        String tenantId = TenantContext.getCurrentTenant();

        // Auto-create the series if it doesn't exist (same pattern as PostingEngineService)
        if (!repository.existsByTenantIdAndSeriesCode(tenantId, seriesCode)) {
            String readableName = seriesCode.replace("_", " ");
            readableName = readableName.substring(0, 1).toUpperCase() + readableName.substring(1).toLowerCase();

            NumberingSeries newSeries = NumberingSeries.builder()
                    .tenantId(tenantId)
                    .seriesCode(seriesCode)
                    .seriesName(readableName)
                    .description("Auto-generated series for " + readableName)
                    .prefix(seriesCode.length() > 3 ? seriesCode.substring(0, 3).toUpperCase() : seriesCode.toUpperCase())
                    .separator("-")
                    .currentNumber(1L)
                    .startNumber(1L)
                    .numberLength(6)
                    .isActive(true)
                    .allowManualOverride(false)
                    .build();
            try {
                repository.saveAndFlush(newSeries);
                log.info("Auto-created numbering series '{}' for tenant '{}'", seriesCode, tenantId);
            } catch (Exception e) {
                log.warn("Failed to auto-create numbering series '{}': {}", seriesCode, e.getMessage());
            }
        }

        // Pessimistic lock to prevent concurrent generation
        NumberingSeries series = repository
                .findByTenantIdAndSeriesCodeForUpdate(tenantId, seriesCode)
                .orElseThrow(() -> new IllegalArgumentException("Numbering series '" + seriesCode + "' not configured for tenant"));

        if (!series.getIsActive()) {
            throw new IllegalArgumentException("Numbering series '" + seriesCode + "' is not active");
        }

        String nextNumber = series.generateNextNumber();
        series.incrementNumber();
        repository.save(series);

        log.info("Generated next number '{}' for series '{}' (tenant '{}')", nextNumber, seriesCode, tenantId);
        return Map.of("seriesCode", seriesCode, "nextNumber", nextNumber);
    }
}
