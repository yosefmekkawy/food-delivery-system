package com.mentorship.food_delivery_app.cart.checkout;

import com.mentorship.food_delivery_app.cart.dto.CheckoutCartRequestDTO;
import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CheckoutContext {

    private final Cart cart;
    private final CheckoutCartRequestDTO request;

    private UUID couponId;
    private BigDecimal discountValue = BigDecimal.ZERO;
    private Order order;
    private PaymentTransaction paymentTransaction;

    public CheckoutContext(Cart cart, CheckoutCartRequestDTO request) {
        this.cart = cart;
        this.request = request;
    }
}
