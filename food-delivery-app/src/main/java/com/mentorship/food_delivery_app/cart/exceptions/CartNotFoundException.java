package com.mentorship.food_delivery_app.cart.exceptions;

public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException(Long customerId) {
        super("Cart not found for customer ID: " + customerId);
    }
}

