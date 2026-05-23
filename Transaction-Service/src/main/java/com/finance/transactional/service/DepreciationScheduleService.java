package com.finance.transactional.service;

import com.finance.transactional.dto.DepreciationScheduleDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.mapper.DepreciationScheduleMapper;
import com.finance.transactional.model.asset.AssetTransaction;
import com.finance.transactional.model.asset.DepreciationSchedule;
import com.finance.transactional.model.asset.FixedAsset;
import com.finance.transactional.repository.AssetTransactionRepository;
import com.finance.transactional.repository.DepreciationScheduleRepository;
import com.finance.transactional.repository.FixedAssetRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepreciationScheduleService {

    private final DepreciationScheduleRepository repository;
    private final DepreciationScheduleMapper mapper;
    private final FixedAssetRepository fixedAssetRepository;
    private final AssetTransactionRepository assetTransactionRepository;

    @Transactional
    public DepreciationScheduleDto createDepreciationSchedule(UUID tenantId, DepreciationScheduleDto dto) {
        DepreciationSchedule depreciationSchedule = mapper.toEntity(dto);
        depreciationSchedule.setTenantId(tenantId);
        if (depreciationSchedule.getIsPosted() == null) {
            depreciationSchedule.setIsPosted(false);
        }
        if (depreciationSchedule.getDepreciationAmount() == null) {
            depreciationSchedule.setDepreciationAmount(calculateDepreciationAmount(depreciationSchedule.getAsset()));
        }
        DepreciationSchedule saved = repository.save(depreciationSchedule);
        return mapper.toDto(saved);
    }

    @Transactional
    public DepreciationScheduleDto updateDepreciationSchedule(UUID tenantId, UUID id, DepreciationScheduleDto dto) {
        DepreciationSchedule existing = getExistingDepreciationSchedule(tenantId, id);
        DepreciationSchedule updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        if (updated.getIsPosted() == null) {
            updated.setIsPosted(existing.getIsPosted());
        }
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public DepreciationScheduleDto getDepreciationScheduleById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingDepreciationSchedule(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<DepreciationScheduleDto> getAllDepreciationSchedules(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteDepreciationSchedule(UUID tenantId, UUID id) {
        DepreciationSchedule depreciationSchedule = getExistingDepreciationSchedule(tenantId, id);
        repository.delete(depreciationSchedule);
    }

    @Transactional
    public Map<String, Object> recalculateSchedule(UUID tenantId, UUID id) {
        DepreciationSchedule schedule = getExistingDepreciationSchedule(tenantId, id);
        schedule.setDepreciationAmount(calculateDepreciationAmount(schedule.getAsset(), schedule.getPeriodStart(), schedule.getPeriodEnd()));
        repository.save(schedule);
        return Map.of("message", "Depreciation schedule recalculated successfully.");
    }

    @Transactional
    public Map<String, Object> postSchedule(UUID tenantId, UUID id) {
        DepreciationSchedule schedule = getExistingDepreciationSchedule(tenantId, id);
        if (Boolean.TRUE.equals(schedule.getIsPosted())) {
            return Map.of("message", "Depreciation schedule is already posted.");
        }

        FixedAsset asset = getExistingAsset(tenantId, schedule.getAsset().getId());
        BigDecimal depreciationAmount = safe(schedule.getDepreciationAmount());
        asset.setAccumulatedDepreciation(safe(asset.getAccumulatedDepreciation()).add(depreciationAmount));
        asset.setNetBookValue(safe(asset.getAcquisitionCost()).subtract(safe(asset.getAccumulatedDepreciation())).subtract(depreciationAmount));
        fixedAssetRepository.save(asset);

        schedule.setIsPosted(true);
        repository.save(schedule);

        AssetTransaction transaction = new AssetTransaction();
        transaction.setTenantId(tenantId);
        transaction.setAsset(asset);
        transaction.setTransactionType("DEPRECIATION");
        transaction.setTransactionDate(LocalDate.now());
        transaction.setAmount(depreciationAmount.negate());
        transaction.setDescription("Posted depreciation for period " + schedule.getPeriodStart() + " to " + schedule.getPeriodEnd());
        assetTransactionRepository.save(transaction);

        return Map.of("message", "Depreciation posted to the asset register successfully.");
    }

    @Transactional
    public Map<String, Object> postAllSchedules(UUID tenantId) {
        List<DepreciationSchedule> schedules = repository.findByTenantIdAndIsPostedFalse(tenantId);
        schedules.forEach(schedule -> postSchedule(tenantId, schedule.getId()));
        return Map.of("message", schedules.isEmpty()
                ? "No draft depreciation schedules were available to post."
                : "All draft depreciation schedules posted successfully.");
    }

    private BigDecimal calculateDepreciationAmount(FixedAsset asset) {
        if (asset == null || asset.getUsefulLifeYears() == null || asset.getUsefulLifeYears() <= 0) {
            return BigDecimal.ZERO;
        }
        return calculateDepreciationAmount(asset, null, null);
    }

    private BigDecimal calculateDepreciationAmount(FixedAsset asset, LocalDate periodStart, LocalDate periodEnd) {
        if (asset == null || asset.getUsefulLifeYears() == null || asset.getUsefulLifeYears() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal depreciableBase = safe(asset.getAcquisitionCost()).subtract(safe(asset.getSalvageValue()));
        if (depreciableBase.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal annual = depreciableBase.divide(BigDecimal.valueOf(asset.getUsefulLifeYears()), 8, RoundingMode.HALF_UP);
        if (periodStart == null || periodEnd == null || periodEnd.isBefore(periodStart)) {
            return annual.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        }

        LocalDate effectiveStart = asset.getAcquisitionDate() != null && asset.getAcquisitionDate().isAfter(periodStart)
                ? asset.getAcquisitionDate()
                : periodStart;
        if (periodEnd.isBefore(effectiveStart)) {
            return BigDecimal.ZERO;
        }

        long daysInPeriod = ChronoUnit.DAYS.between(effectiveStart, periodEnd) + 1;
        BigDecimal daily = annual.divide(BigDecimal.valueOf(365), 8, RoundingMode.HALF_UP);
        return daily.multiply(BigDecimal.valueOf(daysInPeriod)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private FixedAsset getExistingAsset(UUID tenantId, UUID id) {
        return fixedAssetRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAsset not found with id " + id));
    }

    private DepreciationSchedule getExistingDepreciationSchedule(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("DepreciationSchedule not found with id " + id));
    }
}
