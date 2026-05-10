package com.mentorship.food_delivery_app.cart.exceptions;

import java.util.UUID;

public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException(UUID customerId) {
        super("Cart not found for customer ID: " + customerId);
    }
}

