package org.example.tay.internassign3.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.tay.internassign3.dto.request.EmployeeRequestDTO;
import org.example.tay.internassign3.dto.response.EmployeeResponseDTO;
import org.example.tay.internassign3.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        log.info("getAllEmployees request");
        return ResponseEntity.ok(employeeService.findAllEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable String id) {
        log.info("getEmployeeById request");
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO employeeRequestDTO) {
        log.info("createEmployee request");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(employeeRequestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody EmployeeRequestDTO request) {
        log.info("updateEmployee request");
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {
        log.info("deleteEmployee request");
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
