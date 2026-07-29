package com.mentorship.food_delivery_app.cart.exceptions;

import java.util.UUID;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException(UUID customerId) {
        super("Cannot checkout an empty cart for customer ID: " + customerId);
    }
}

