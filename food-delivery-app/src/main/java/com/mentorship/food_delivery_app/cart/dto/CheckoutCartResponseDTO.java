package com.mentorship.food_delivery_app.cart.dto;

import com.mentorship.food_delivery_app.order.dto.OrderResponseDTO;
import com.mentorship.food_delivery_app.payment.dto.PaymentTransactionResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutCartResponseDTO {

    private OrderResponseDTO order;
    private PaymentTransactionResponseDTO payment;
}

