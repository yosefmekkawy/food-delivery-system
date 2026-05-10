package com.mentorship.food_delivery_app.payment.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface PaymentGateway {

    GatewayResult process(PaymentRequest request, String configDetails);

    record PaymentRequest(UUID orderId,
                          UUID customerId,
                          UUID restaurantBranchId,
                          String paymentIntegrationType,
                          BigDecimal amount) {
    }

    record GatewayResult(String status, LocalDateTime processedAt) {
    }
}

