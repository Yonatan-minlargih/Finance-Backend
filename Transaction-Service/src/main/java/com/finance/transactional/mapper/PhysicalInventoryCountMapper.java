package com.finance.transactional.mapper;

import com.finance.transactional.dto.PhysicalInventoryCountDto;
import com.finance.transactional.model.asset.FixedAsset;
import com.finance.transactional.model.asset.PhysicalInventoryCount;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PhysicalInventoryCountMapper {
    @Mapping(source = "asset.id", target = "assetId")
    PhysicalInventoryCountDto toDto(PhysicalInventoryCount entity);

    @Mapping(source = "assetId", target = "asset")
    PhysicalInventoryCount toEntity(PhysicalInventoryCountDto dto);

    default FixedAsset mapFixedAsset(UUID id) {
        if (id == null) {
            return null;
        }
        FixedAsset entity = new FixedAsset();
        entity.setId(id);
        return entity;
    }
}
