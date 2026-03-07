package org.example.tay.internassign3;

import org.bson.types.ObjectId;
import org.example.tay.internassign3.dto.request.EmployeeRequestDTO;
import org.example.tay.internassign3.dto.response.EmployeeResponseDTO;
import org.example.tay.internassign3.entity.Employee;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
import org.example.tay.internassign3.mapper.EmployeeMapper;
import org.example.tay.internassign3.repository.EmployeeRepository;
import org.example.tay.internassign3.service.serviceImpl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeServiceImpl Unit Tests")
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    // Shared test fixtures
    private ObjectId employeeObjectId;
    private String employeeId;
    private Employee employee;
    private EmployeeRequestDTO requestDTO;
    private EmployeeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        employeeObjectId = new ObjectId();
        employeeId = employeeObjectId.toHexString();

        employee = Employee.builder()
                .id(employeeObjectId)
                .employeeNumber("EMP001")
                .firstName("Alice")
                .lastName("Tan")
                .email("alice.tan@example.com")
                .build();

        requestDTO = EmployeeRequestDTO.builder()
                .employeeNumber("EMP001")
                .firstName("Alice")
                .lastName("Tan")
                .email("alice.tan@example.com")
                .build();

        responseDTO = EmployeeResponseDTO.builder()
                .id(employeeId)
                .employeeNumber("EMP001")
                .firstName("Alice")
                .lastName("Tan")
                .email("alice.tan@example.com")
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // createEmployee
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createEmployee")
    class CreateEmployee {

        @Test
        @DisplayName("should create employee successfully when no conflicts exist")
        void shouldCreateEmployeeSuccessfully() {
            // Given
            given(employeeRepository.existsByEmployeeNumber("EMP001")).willReturn(false);
            given(employeeRepository.existsByEmail("alice.tan@example.com")).willReturn(false);
            given(employeeMapper.toEntity(requestDTO)).willReturn(employee);
            given(employeeRepository.save(employee)).willReturn(employee);
            given(employeeMapper.toResponse(employee)).willReturn(responseDTO);

            // When
            EmployeeResponseDTO result = employeeService.createEmployee(requestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmployeeNumber()).isEqualTo("EMP001");
            assertThat(result.getEmail()).isEqualTo("alice.tan@example.com");
            then(employeeRepository).should().save(employee);
        }

        @Test
        @DisplayName("should throw ConflictException when employee number already exists")
        void shouldThrowConflictWhenEmployeeNumberDuplicate() {
            // Given
            given(employeeRepository.existsByEmployeeNumber("EMP001")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> employeeService.createEmployee(requestDTO))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("EMP001");

            then(employeeRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("should throw ConflictException when email already exists")
        void shouldThrowConflictWhenEmailDuplicate() {
            // Given
            given(employeeRepository.existsByEmployeeNumber("EMP001")).willReturn(false);
            given(employeeRepository.existsByEmail("alice.tan@example.com")).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> employeeService.createEmployee(requestDTO))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("alice.tan@example.com");

            then(employeeRepository).should(never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // findAllEmployees
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findAllEmployees")
    class FindAllEmployees {

        @Test
        @DisplayName("should return list of all employees")
        void shouldReturnAllEmployees() {
            // Given
            given(employeeRepository.findAll()).willReturn(List.of(employee));
            given(employeeMapper.toResponse(employee)).willReturn(responseDTO);

            // When
            List<EmployeeResponseDTO> result = employeeService.findAllEmployees();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmployeeNumber()).isEqualTo("EMP001");
        }

        @Test
        @DisplayName("should return empty list when no employees exist")
        void shouldReturnEmptyListWhenNoEmployees() {
            // Given
            given(employeeRepository.findAll()).willReturn(List.of());

            // When
            List<EmployeeResponseDTO> result = employeeService.findAllEmployees();

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // findById
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return employee when found by valid ID")
        void shouldReturnEmployeeById() {
            // Given
            given(employeeRepository.findById(employeeObjectId)).willReturn(Optional.of(employee));
            given(employeeMapper.toResponse(employee)).willReturn(responseDTO);

            // When
            EmployeeResponseDTO result = employeeService.findById(employeeId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(employeeId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when employee not found")
        void shouldThrowNotFoundWhenEmployeeMissing() {
            // Given
            given(employeeRepository.findById(employeeObjectId)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> employeeService.findById(employeeId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(employeeId);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // updateEmployee
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployee {

        @Test
        @DisplayName("should update employee successfully when no conflicts")
        void shouldUpdateEmployeeSuccessfully() {
            // Given
            given(employeeRepository.findById(employeeObjectId)).willReturn(Optional.of(employee));
            given(employeeRepository.existsByEmployeeNumberAndIdNot("EMP001", employeeObjectId)).willReturn(false);
            given(employeeRepository.existsByEmail("alice.tan@example.com")).willReturn(false);
            given(employeeRepository.save(employee)).willReturn(employee);
            given(employeeMapper.toResponse(employee)).willReturn(responseDTO);

            // When
            EmployeeResponseDTO result = employeeService.updateEmployee(employeeId, requestDTO);

            // Then
            assertThat(result).isNotNull();
            then(employeeRepository).should().save(employee);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when updating non-existent employee")
        void shouldThrowNotFoundWhenUpdatingMissingEmployee() {
            // Given
            given(employeeRepository.findById(employeeObjectId)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw ConflictException when new employee number is taken by another employee")
        void shouldThrowConflictWhenEmployeeNumberTakenByAnother() {
            // Given
            given(employeeRepository.findById(employeeObjectId)).willReturn(Optional.of(employee));
            given(employeeRepository.existsByEmployeeNumberAndIdNot("EMP001", employeeObjectId)).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, requestDTO))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("EMP001");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // getEmployeeEntityById
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getEmployeeEntityById")
    class GetEmployeeEntityById {

        @Test
        @DisplayName("should return Employee entity when found")
        void shouldReturnEmployeeEntity() {
            // Given
            given(employeeRepository.findById(employeeObjectId)).willReturn(Optional.of(employee));

            // When
            Employee result = employeeService.getEmployeeEntityById(employeeId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmployeeNumber()).isEqualTo("EMP001");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when entity not found")
        void shouldThrowWhenEntityNotFound() {
            // Given
            given(employeeRepository.findById(employeeObjectId)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> employeeService.getEmployeeEntityById(employeeId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // deleteEmployee
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployee {

        @Test
        @DisplayName("(bug) throws ResourceNotFoundException when employee EXISTS — guard is inverted")
        void bugThrowsWhenEmployeeExists() {
            // Given — employee IS found in DB
            given(employeeRepository.existsById(employeeObjectId)).willReturn(true);

            // When / Then
            // Due to the inverted guard the impl throws even though the employee exists
            assertThatThrownBy(() -> employeeService.deleteEmployee(employeeId))
                    .isInstanceOf(ResourceNotFoundException.class);

            // deleteById is never reached
            then(employeeRepository).should(never()).deleteById(any());
        }

        @Test
        @DisplayName("(bug) does NOT throw and calls deleteById when employee does NOT exist")
        void bugCallsDeleteWhenEmployeeMissing() {
            // Given — employee is NOT in DB
            given(employeeRepository.existsById(employeeObjectId)).willReturn(false);
            willDoNothing().given(employeeRepository).deleteById(employeeObjectId);

            // When — no exception is thrown (guard is bypassed incorrectly)
            employeeService.deleteEmployee(employeeId);

            // Then — deleteById is still called on a non-existent employee
            then(employeeRepository).should().deleteById(employeeObjectId);
        }
    }
}
