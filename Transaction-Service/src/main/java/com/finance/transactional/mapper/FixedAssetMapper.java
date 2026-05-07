package com.finance.transactional.mapper;

import com.finance.transactional.model.asset.FixedAsset;
import com.finance.transactional.dto.FixedAssetDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FixedAssetMapper {
    FixedAssetDto toDto(FixedAsset entity);
    FixedAsset toEntity(FixedAssetDto dto);
}
