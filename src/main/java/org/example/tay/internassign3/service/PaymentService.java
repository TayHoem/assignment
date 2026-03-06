package org.example.tay.internassign3.service;

import org.example.tay.internassign3.dto.request.PaymentRequestDTO;
import org.example.tay.internassign3.dto.response.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {
    PaymentResponseDTO createPayment(String employeeId, String claimId, PaymentRequestDTO request);

    List<PaymentResponseDTO> getPayment();
}
