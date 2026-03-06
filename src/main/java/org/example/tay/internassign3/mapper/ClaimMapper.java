package org.example.tay.internassign3.mapper;

import org.example.tay.internassign3.dto.ClaimItemDto;
import org.example.tay.internassign3.dto.ClaimTypeDto;
import org.example.tay.internassign3.dto.request.ClaimRequestDTO;
import org.example.tay.internassign3.dto.response.ClaimResponseDTO;
import org.example.tay.internassign3.entity.Claim;
import org.example.tay.internassign3.entity.ClaimItem;
import org.example.tay.internassign3.entity.ClaimType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

    @Mapping(target = "id", expression = "java(entity.getId().toHexString())")
//    @Mapping(target = "employeeId", expression = "java(entity.getEmployee().getId().toHexString())")
    ClaimResponseDTO toResponse(Claim entity);

    ClaimItem toClaimItem(ClaimItemDto dto);

    ClaimType toClaimType(ClaimTypeDto dto);
}
