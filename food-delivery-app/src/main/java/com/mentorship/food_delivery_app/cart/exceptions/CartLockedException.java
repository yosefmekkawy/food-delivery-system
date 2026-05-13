package com.mentorship.food_delivery_app.cart.exceptions;

import java.util.UUID;

public class CartLockedException extends RuntimeException {

    public CartLockedException(UUID cartId, String action) {
        super("Cart " + cartId + " is locked and cannot be " + action);
    }
}

