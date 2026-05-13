package com.mentorship.food_delivery_app.payment.gateway;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class SimulatedPaymentGatewayTest {

    @Test
    void process_shouldUseDefaultCompletedStatus() {
        SimulatedPaymentGateway gateway = new SimulatedPaymentGateway();

        PaymentGateway.GatewayResult result = gateway.process(
                new PaymentGateway.PaymentRequest(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    UUID.fromString("22222222-2222-2222-2222-222222222222"),
                    UUID.fromString("33333333-3333-3333-3333-333333333333"),
                    "CARD",
                    new BigDecimal("15.00")
                ),
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
                new PaymentGateway.PaymentRequest(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    UUID.fromString("22222222-2222-2222-2222-222222222222"),
                    UUID.fromString("33333333-3333-3333-3333-333333333333"),
                    "CARD",
                    new BigDecimal("15.00")
                ),
                "{}"
        );

        assertThat(result.status()).isEqualTo("AUTHORIZED");
    }
}

