package com.mentorship.food_delivery_app.customer.exceptions;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(UUID customerId) {
        super("Customer not found with id: " + customerId);
    }
}
