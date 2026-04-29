package com.mentorship.food_delivery_app.payment.gateway;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public GatewayResult process(PaymentRequest request, String configDetails) {
        return new GatewayResult(completedStatus(), LocalDateTime.now());
    }

    protected String completedStatus() {
        return "COMPLETED";
    }
}


