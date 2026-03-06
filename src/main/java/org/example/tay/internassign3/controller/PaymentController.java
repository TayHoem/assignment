package org.example.tay.internassign3.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tay.internassign3.dto.request.PaymentRequestDTO;
import org.example.tay.internassign3.dto.response.PaymentResponseDTO;
import org.example.tay.internassign3.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/employees/{employeeId}/claims/{claimId}/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @PathVariable String employeeId,
            @PathVariable String claimId,
            @RequestBody PaymentRequestDTO paymentRequest
    ) {
        log.debug("Received request to create payment for employee: {} claim: {}", employeeId, claimId);
        PaymentResponseDTO response = paymentService.createPayment(employeeId, claimId, paymentRequest);
        log.info("Payment created successfully for employee: {} claim: {}", employeeId, claimId);
        return ResponseEntity.ok(response);
    }

}
