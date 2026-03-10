package org.example.tay.internassign3.mapper;

import org.example.tay.internassign3.dto.response.ApprovalResponseDTO;
import org.example.tay.internassign3.entity.Approval;
import org.example.tay.internassign3.entityEnum.ApprovalStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApprovalMapper {
   @Mapping(target = "id", expression = "java(entity.getId().toHexString())")
   @Mapping(target = "claimId", expression = "java(entity.getClaimId().toHexString())")
   ApprovalResponseDTO toResponse(Approval entity);

   //make String Status to Enum ApprovalStatus
   ApprovalStatus toApprovalStatus(String status);
}
