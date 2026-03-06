package org.example.tay.internassign3.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.tay.internassign3.dto.request.EmployeeRequestDTO;
import org.example.tay.internassign3.dto.response.EmployeeResponseDTO;
import org.example.tay.internassign3.entity.Employee;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
import org.example.tay.internassign3.mapper.EmployeeMapper;
import org.example.tay.internassign3.repository.EmployeeRepository;
import org.example.tay.internassign3.service.EmployeeService;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        log.info("createEmployee: {}", request.getEmployeeNumber());

        if (employeeRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new ConflictException("Employee number already exists: " + request.getEmployeeNumber());
        }
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }
        Employee employee = employeeMapper.toEntity(request);
        Employee saved = employeeRepository.save(employee);
        log.info("Created employee: {}", saved.getId());

        return employeeMapper.toResponse(saved);
    }

    @Override
    public List<EmployeeResponseDTO> findAllEmployees() {
        log.info("findAll");
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Override
    public EmployeeResponseDTO findById(String id) {
        log.info("findById: {}", id);
        return employeeRepository.findById(new ObjectId(id))
                .map(employeeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    //update employee details
    @Override
    public EmployeeResponseDTO updateEmployee(String id, EmployeeRequestDTO request) {
        log.info("updateEmployee: {}", id);
        Employee employee = employeeRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (employeeRepository.existsByEmployeeNumberAndIdNot(request.getEmployeeNumber(), new ObjectId(id))) {
            throw new ConflictException("Employee number already exists: " + request.getEmployeeNumber());
        }
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        Employee updated = employeeRepository.save(employee);
        log.info("Updated employee: {}", updated.getId());

        return employeeMapper.toResponse(updated);
    }

    //delete employee by id
    @Override
    public void deleteEmployee(String id) {
        log.info("deleteEmployee: {}", id);
        if (employeeRepository.existsById(new ObjectId(id))) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(new ObjectId(id));
        log.info("deleted employee: {}", id);
    }

    @Override
    public Employee getEmployeeEntityById(String id) {
        return employeeRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }
}
