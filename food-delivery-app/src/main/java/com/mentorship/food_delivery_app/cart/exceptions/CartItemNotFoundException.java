package com.mentorship.food_delivery_app.cart.exceptions;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long customerId, Long menuItemId) {
        super("Menu item " + menuItemId + " was not found in the cart for customer ID: " + customerId);
    }
}

