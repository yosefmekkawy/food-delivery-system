package com.mentorship.food_delivery_app.cart.checkout;

public interface CheckoutHandler {
    void setNext(CheckoutHandler next);
    void handle(CheckoutContext context);
}
