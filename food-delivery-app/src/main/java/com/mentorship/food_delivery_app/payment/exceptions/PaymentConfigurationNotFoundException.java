package com.mentorship.food_delivery_app.payment.exceptions;

public class PaymentConfigurationNotFoundException extends RuntimeException {

    public PaymentConfigurationNotFoundException(String paymentIntegrationType) {
        super("Payment integration type '" + paymentIntegrationType + "' is not configured");
    }
}

