package org.example.tay.internassign3.mappers;

import org.example.tay.internassign3.dto.response.PaymentResponseDTO;
import org.example.tay.internassign3.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "id", expression = "java(entity.getId().toHexString())")
    @Mapping(target = "claimId", expression = "java(entity.getClaimId().toHexString())")
    PaymentResponseDTO toResponse(Payment entity);
}
