package com.mentorship.food_delivery_app.payment.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.mentorship.food_delivery_app.customer.entity.Customer;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.payment.dto.PaymentTransactionResponseDTO;
import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;

public interface PaymentService {

    PaymentTransaction processPayment(PaymentCommand command);

    PaymentTransactionResponseDTO toResponse(PaymentTransaction transaction);

    record PaymentCommand(Order order,
                          Customer customer,
                          UUID restaurantBranchId,
                          String paymentIntegrationType,
                          BigDecimal amount) {
    }
}

