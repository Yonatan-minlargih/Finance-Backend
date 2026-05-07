package com.finance.transactional.mapper;

import com.finance.transactional.dto.DepreciationScheduleDto;
import com.finance.transactional.model.asset.DepreciationSchedule;
import com.finance.transactional.model.asset.FixedAsset;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepreciationScheduleMapper {
    @Mapping(source = "asset.id", target = "assetId")
    DepreciationScheduleDto toDto(DepreciationSchedule entity);

    @Mapping(source = "assetId", target = "asset")
    DepreciationSchedule toEntity(DepreciationScheduleDto dto);

    default FixedAsset mapFixedAsset(UUID id) {
        if (id == null) {
            return null;
        }
        FixedAsset entity = new FixedAsset();
        entity.setId(id);
        return entity;
    }
}
