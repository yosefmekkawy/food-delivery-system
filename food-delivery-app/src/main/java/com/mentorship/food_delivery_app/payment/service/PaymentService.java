package com.mentorship.food_delivery_app.payment.service;

import com.mentorship.food_delivery_app.customer.entity.Customer;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.payment.dto.PaymentTransactionResponseDTO;
import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;

import java.math.BigDecimal;

public interface PaymentService {

    PaymentTransaction processPayment(PaymentCommand command);

    PaymentTransactionResponseDTO toResponse(PaymentTransaction transaction);

    record PaymentCommand(Order order,
                          Customer customer,
                          Long restaurantBranchId,
                          String paymentIntegrationType,
                          BigDecimal amount) {
    }
}

