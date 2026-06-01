package com.mentorship.food_delivery_app.order.service;

import com.mentorship.food_delivery_app.cart.checkout.CheckoutContext;
import com.mentorship.food_delivery_app.cart.checkout.CheckoutHandler;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartResponseDTO;
import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CheckoutHandler checkoutChain;
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Transactional
    public CheckoutCartResponseDTO checkout(Cart cart, CheckoutCartRequestDTO request) {
        CheckoutContext context = new CheckoutContext(cart, request);
        checkoutChain.handle(context);
        return new CheckoutCartResponseDTO(
                orderService.toResponse(context.getOrder()),
                paymentService.toResponse(context.getPaymentTransaction())
        );
    }
}

