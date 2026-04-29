package com.mentorship.food_delivery_app.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionResponseDTO {

    private UUID transactionId;
    private String status;
    private String paymentIntegrationType;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
}

