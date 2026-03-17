package org.example.tay.internassign3.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.tay.internassign3.exception.BadRequestException;
import org.example.tay.internassign3.mapper.EmployeeMapper;
import org.example.tay.internassign3.dto.request.EmployeeRequestDTO;
import org.example.tay.internassign3.dto.response.EmployeeResponseDTO;
import org.example.tay.internassign3.entity.Employee;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
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

    //update employee details
    @Override
    public EmployeeResponseDTO updateEmployee(String id, EmployeeRequestDTO request) {
        log.info("updateEmployee: {}", id);
        if (request == null){
            throw new BadRequestException("Update Employee Request cannot be null");
        }

        // 1. Convert ID and find existing employee
        ObjectId objectId = new ObjectId(id);
        Employee employee = employeeRepository.findById(objectId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // 2. Partial Update for Employee Number (with duplicate check)
        if (request.getEmployeeNumber() != null && !request.getEmployeeNumber().isBlank()) {
            if (employeeRepository.existsByEmployeeNumberAndIdNot(request.getEmployeeNumber(), objectId)) {
                throw new ConflictException("Employee number already exists: " + request.getEmployeeNumber());
            }
            employee.setEmployeeNumber(request.getEmployeeNumber());
        }

        // 3. Partial Update for First Name
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            employee.setFirstName(request.getFirstName());
        }

        // 4. Partial Update for Last Name
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            employee.setLastName(request.getLastName());
        }

        // 5. Partial Update for Email (with duplicate check)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (employeeRepository.existsByEmailAndIdNot(request.getEmail(), objectId)) {
                throw new ConflictException("Email already exists: " + request.getEmail());
            }
            employee.setEmail(request.getEmail());
        }

        // 6. Save the entity and return the response
        Employee saved = employeeRepository.save(employee);
        log.info("Successfully updated fields for employee: {}", saved.getId());

        return employeeMapper.toResponse(saved);
    }

    @Override
    public EmployeeResponseDTO findEmployeesByEmployeeNumber(String employeeNumber){
        return employeeRepository.findByEmployeeNumber(employeeNumber)
                .map(employeeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with employee number" + employeeNumber));
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

    @Override
    public Employee getEmployeeEntityById(String id) {
        return employeeRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    //delete employee by id
    @Override
    public void deleteEmployee(String id) {
        log.info("deleteEmployee: {}", id);
        if (!employeeRepository.existsById(new ObjectId(id))) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(new ObjectId(id));
        log.info("deleted employee: {}", id);
    }
}
