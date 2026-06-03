package com.mentorship.food_delivery_app.cart.checkout.handlers;

import com.mentorship.food_delivery_app.cart.checkout.AbstractCheckoutHandler;
import com.mentorship.food_delivery_app.cart.checkout.CheckoutContext;
import com.mentorship.food_delivery_app.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentProcessingHandler extends AbstractCheckoutHandler {

    private final PaymentService paymentService;

    @Override
    public void handle(CheckoutContext context) {
        context.setPaymentTransaction(paymentService.processPayment(
                new PaymentService.PaymentCommand(
                        context.getOrder(),
                        context.getCart().getCustomer(),
                        context.getCart().getRestaurantId(),
                        context.getRequest().getPaymentIntegrationType(),
                        context.getOrder().getTotal()
                )
        ));
        proceed(context);
    }
}
