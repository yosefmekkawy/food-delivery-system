package com.mentorship.food_delivery_app.payment.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentGateway {

    GatewayResult process(PaymentRequest request, String configDetails);

    record PaymentRequest(Long orderId,
                          Long customerId,
                          Long restaurantBranchId,
                          String paymentIntegrationType,
                          BigDecimal amount) {
    }

    record GatewayResult(String status, LocalDateTime processedAt) {
    }
}

