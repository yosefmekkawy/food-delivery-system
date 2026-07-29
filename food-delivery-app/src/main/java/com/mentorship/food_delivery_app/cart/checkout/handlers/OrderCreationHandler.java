package com.mentorship.food_delivery_app.cart.checkout.handlers;

import com.mentorship.food_delivery_app.cart.checkout.AbstractCheckoutHandler;
import com.mentorship.food_delivery_app.cart.checkout.CheckoutContext;
import com.mentorship.food_delivery_app.order.service.OrderService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderCreationHandler extends AbstractCheckoutHandler {

    private final OrderService orderService;

    @Override
    public void handle(CheckoutContext context) {
        context.setOrder(orderService.createOrderFromCart(
                context.getCart(),
                context.getRequest().getNote(),
                context.getCouponId(),
                context.getDiscountValue()
        ));
        proceed(context);
    }
}
