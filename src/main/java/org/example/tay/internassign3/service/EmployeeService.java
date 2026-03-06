package org.example.tay.internassign3.service;

import org.example.tay.internassign3.dto.request.EmployeeRequestDTO;
import org.example.tay.internassign3.dto.response.EmployeeResponseDTO;
import org.example.tay.internassign3.entity.Employee;

import java.util.List;

public interface EmployeeService {

    EmployeeResponseDTO createEmployee(EmployeeRequestDTO request);

    List<EmployeeResponseDTO> findAllEmployees();

    EmployeeResponseDTO findById(String id);

    //update employee details
    EmployeeResponseDTO updateEmployee(String id, EmployeeRequestDTO request);

    //delete employee by id
    void deleteEmployee(String id);

    Employee getEmployeeEntityById(String id);
}
