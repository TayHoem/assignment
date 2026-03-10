package org.example.tay.internassign3.mapper;

import org.example.tay.internassign3.dto.request.EmployeeRequestDTO;
import org.example.tay.internassign3.dto.response.EmployeeResponseDTO;
import org.example.tay.internassign3.entity.Employee;
import org.example.tay.internassign3.entity.EmployeeSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeSnapshot toEmployeeSnapshot(Employee employee);

    Employee toEntity(EmployeeRequestDTO dto);

    @Mapping(target = "id", expression = "java(entity.getId().toHexString())")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    EmployeeResponseDTO toResponse(Employee entity);
}
