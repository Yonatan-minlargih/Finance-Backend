package com.finance.transactional.mapper;

import com.finance.transactional.model.system.ApprovalWorkflow;
import com.finance.transactional.dto.ApprovalWorkflowDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApprovalWorkflowMapper {
    ApprovalWorkflowDto toDto(ApprovalWorkflow entity);
    ApprovalWorkflow toEntity(ApprovalWorkflowDto dto);
}
