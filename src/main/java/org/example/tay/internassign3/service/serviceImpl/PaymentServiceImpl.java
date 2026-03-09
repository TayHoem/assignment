package org.example.tay.internassign3.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.tay.internassign3.dto.request.PaymentRequestDTO;
import org.example.tay.internassign3.dto.response.PaymentResponseDTO;
import org.example.tay.internassign3.entity.Claim;
import org.example.tay.internassign3.entity.Employee;
import org.example.tay.internassign3.entity.Payment;
import org.example.tay.internassign3.entityEnum.ClaimStatus;
import org.example.tay.internassign3.entityEnum.PaymentStatus;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.mappers.PaymentMapper;
import org.example.tay.internassign3.repository.PaymentRepository;
import org.example.tay.internassign3.service.ClaimService;
import org.example.tay.internassign3.service.EmployeeService;
import org.example.tay.internassign3.service.PaymentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ClaimService claimService;
    private final EmployeeService employeeService;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponseDTO createPayment(String employeeId, String claimId, PaymentRequestDTO request) {
        log.debug("Creating payment for claim: {} employee: {}", claimId, employeeId);
        Employee employee = employeeService.getEmployeeEntityById(employeeId);
        Claim claim = claimService.getClaimEntityById(claimId);

        // Validate claim belongs to employee
        if (!claim.getEmployeeNumber().equals(employee.getEmployeeNumber())) {
            throw new ConflictException("Claim does not belong to the specified employee");
        }

        // Only approved claims can be paid
        if (!ClaimStatus.APPROVED.equals(claim.getStatus())) {
            throw new ConflictException("Payment can only be created for APPROVED claims. Current status: " + claim.getStatus());
        }

        // Prevent duplicate payments
        if (paymentRepository.existsByClaimId(new ObjectId(claimId))) {
            throw new ConflictException("Payment already exists for claim: " + claimId);
        }

        Payment payment = Payment.builder()
                .claimId(new ObjectId(claimId))
                .employeeNumber(claim.getEmployeeNumber())
                .paymentMethod(request.getPaymentMethod())
                .paymentAmount(claim.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Created payment: {} for claim: {}", saved.getId(), claimId);
        return paymentMapper.toResponse(saved);
    }

    @Override
    public List<PaymentResponseDTO> getPayment(){
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
}
