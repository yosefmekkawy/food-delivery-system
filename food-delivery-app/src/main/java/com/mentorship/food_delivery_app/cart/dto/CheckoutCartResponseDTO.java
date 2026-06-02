package com.mentorship.food_delivery_app.cart.dto;

import com.mentorship.food_delivery_app.order.dto.OrderResponseDTO;
import com.mentorship.food_delivery_app.payment.dto.PaymentTransactionResponseDTO;

public class CheckoutCartResponseDTO {

    private OrderResponseDTO order;
    private PaymentTransactionResponseDTO payment;

    public CheckoutCartResponseDTO() {
    }

    public CheckoutCartResponseDTO(OrderResponseDTO order, PaymentTransactionResponseDTO payment) {
        this.order = order;
        this.payment = payment;
    }

    public OrderResponseDTO getOrder() {
        return order;
    }

    public void setOrder(OrderResponseDTO order) {
        this.order = order;
    }

    public PaymentTransactionResponseDTO getPayment() {
        return payment;
    }

    public void setPayment(PaymentTransactionResponseDTO payment) {
        this.payment = payment;
    }
}

