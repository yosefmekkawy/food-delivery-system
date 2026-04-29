package com.mentorship.food_delivery_app.payment.gateway;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedPaymentGatewayTest {

    @Test
    void process_shouldUseDefaultCompletedStatus() {
        SimulatedPaymentGateway gateway = new SimulatedPaymentGateway();

        PaymentGateway.GatewayResult result = gateway.process(
                new PaymentGateway.PaymentRequest(1L, 2L, 3L, "CARD", new BigDecimal("15.00")),
                "{}"
        );

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.processedAt()).isNotNull();
    }

    @Test
    void process_shouldAllowCompletedStatusOverride() {
        SimulatedPaymentGateway gateway = new SimulatedPaymentGateway() {
            @Override
            protected String completedStatus() {
                return "AUTHORIZED";
            }
        };

        PaymentGateway.GatewayResult result = gateway.process(
                new PaymentGateway.PaymentRequest(1L, 2L, 3L, "CARD", new BigDecimal("15.00")),
                "{}"
        );

        assertThat(result.status()).isEqualTo("AUTHORIZED");
    }
}

