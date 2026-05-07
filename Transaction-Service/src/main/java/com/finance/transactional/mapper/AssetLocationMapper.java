package com.finance.transactional.mapper;

import com.finance.transactional.dto.AssetLocationDto;
import com.finance.transactional.model.asset.AssetLocation;
import com.finance.transactional.model.asset.FixedAsset;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AssetLocationMapper {
    @Mapping(source = "asset.id", target = "assetId")
    AssetLocationDto toDto(AssetLocation entity);

    @Mapping(source = "assetId", target = "asset")
    AssetLocation toEntity(AssetLocationDto dto);

    default FixedAsset mapFixedAsset(UUID id) {
        if (id == null) {
            return null;
        }
        FixedAsset entity = new FixedAsset();
        entity.setId(id);
        return entity;
    }
}
