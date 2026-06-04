package com.financial.corefinance.mapper;

import com.financial.corefinance.domain.entity.NumberingSeries;
import com.financial.corefinance.dto.request.NumberingSeriesRequest;
import com.financial.corefinance.dto.response.NumberingSeriesResponse;
import org.springframework.stereotype.Component;

@Component
public class NumberingSeriesMapper {

    public NumberingSeries toEntity(NumberingSeriesRequest request) {
        if (request == null) return null;
        
        NumberingSeries entity = new NumberingSeries();
        entity.setSeriesCode(request.getSeriesCode());
        entity.setSeriesName(request.getSeriesName());
        entity.setDescription(request.getDescription());
        entity.setPrefix(request.getPrefix());
        entity.setSuffix(request.getSuffix());
        if(request.getCurrentNumber() != null) entity.setCurrentNumber(request.getCurrentNumber());
        if(request.getStartNumber() != null) entity.setStartNumber(request.getStartNumber());
        entity.setEndNumber(request.getEndNumber());
        if(request.getNumberLength() != null) entity.setNumberLength(request.getNumberLength());
        entity.setResetFrequency(request.getResetFrequency());
        if(request.getSeparator() != null) entity.setSeparator(request.getSeparator());
        if(request.getIsActive() != null) entity.setIsActive(request.getIsActive());
        if(request.getAllowManualOverride() != null) entity.setAllowManualOverride(request.getAllowManualOverride());
        return entity;
    }

    public void updateEntity(NumberingSeries entity, NumberingSeriesRequest request) {
        if (request == null) return;
        
        entity.setSeriesCode(request.getSeriesCode());
        entity.setSeriesName(request.getSeriesName());
        entity.setDescription(request.getDescription());
        entity.setPrefix(request.getPrefix());
        entity.setSuffix(request.getSuffix());
        if(request.getCurrentNumber() != null) entity.setCurrentNumber(request.getCurrentNumber());
        if(request.getStartNumber() != null) entity.setStartNumber(request.getStartNumber());
        entity.setEndNumber(request.getEndNumber());
        if(request.getNumberLength() != null) entity.setNumberLength(request.getNumberLength());
        entity.setResetFrequency(request.getResetFrequency());
        if(request.getSeparator() != null) entity.setSeparator(request.getSeparator());
        if(request.getIsActive() != null) entity.setIsActive(request.getIsActive());
        if(request.getAllowManualOverride() != null) entity.setAllowManualOverride(request.getAllowManualOverride());
        // force format regeneration
        entity.setFormat(null);
    }

    public NumberingSeriesResponse toResponse(NumberingSeries entity) {
        if (entity == null) return null;
        
        return NumberingSeriesResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .seriesCode(entity.getSeriesCode())
                .seriesName(entity.getSeriesName())
                .description(entity.getDescription())
                .prefix(entity.getPrefix())
                .suffix(entity.getSuffix())
                .currentNumber(entity.getCurrentNumber())
                .startNumber(entity.getStartNumber())
                .endNumber(entity.getEndNumber())
                .numberLength(entity.getNumberLength())
                .resetFrequency(entity.getResetFrequency())
                .separator(entity.getSeparator())
                .format(entity.getFormat())
                .isActive(entity.getIsActive())
                .allowManualOverride(entity.getAllowManualOverride())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
