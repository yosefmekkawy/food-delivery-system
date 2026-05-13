package com.mentorship.food_delivery_app.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryPaymentDTO {

    private UUID transactionId;
    private String paymentType;
    private String status;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
}
