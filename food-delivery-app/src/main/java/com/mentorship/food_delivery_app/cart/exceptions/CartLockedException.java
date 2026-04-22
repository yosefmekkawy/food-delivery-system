package com.mentorship.food_delivery_app.cart.exceptions;

public class CartLockedException extends RuntimeException {

    public CartLockedException(Long cartId, String action) {
        super("Cart " + cartId + " is locked and cannot be " + action);
    }
}

