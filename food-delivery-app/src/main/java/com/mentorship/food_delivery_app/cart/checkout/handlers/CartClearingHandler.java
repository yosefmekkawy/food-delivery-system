package com.mentorship.food_delivery_app.cart.checkout.handlers;

import com.mentorship.food_delivery_app.cart.checkout.AbstractCheckoutHandler;
import com.mentorship.food_delivery_app.cart.checkout.CheckoutContext;

public class CartClearingHandler extends AbstractCheckoutHandler {

    @Override
    public void handle(CheckoutContext context) {
        context.getCart().clearItems();
        context.getCart().setRestaurantId(null);
        proceed(context);
    }
}
