package com.finance.transactional.mapper;

import com.finance.transactional.dto.AssetTransactionDto;
import com.finance.transactional.model.asset.AssetTransaction;
import com.finance.transactional.model.asset.FixedAsset;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AssetTransactionMapper {
    @Mapping(source = "asset.id", target = "assetId")
    AssetTransactionDto toDto(AssetTransaction entity);

    @Mapping(source = "assetId", target = "asset")
    AssetTransaction toEntity(AssetTransactionDto dto);

    default FixedAsset mapFixedAsset(UUID id) {
        if (id == null) {
            return null;
        }
        FixedAsset entity = new FixedAsset();
        entity.setId(id);
        return entity;
    }
}
