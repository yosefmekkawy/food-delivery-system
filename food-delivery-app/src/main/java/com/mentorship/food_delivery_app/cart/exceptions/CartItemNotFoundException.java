package com.mentorship.food_delivery_app.cart.exceptions;

import java.util.UUID;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(UUID customerId, UUID menuItemId) {
        super("Menu item " + menuItemId + " was not found in the cart for customer ID: " + customerId);
    }
}

