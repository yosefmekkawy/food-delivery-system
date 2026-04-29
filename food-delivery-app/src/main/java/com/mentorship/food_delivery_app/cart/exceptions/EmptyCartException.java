package com.mentorship.food_delivery_app.cart.exceptions;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException(Long customerId) {
        super("Cannot checkout an empty cart for customer ID: " + customerId);
    }
}

