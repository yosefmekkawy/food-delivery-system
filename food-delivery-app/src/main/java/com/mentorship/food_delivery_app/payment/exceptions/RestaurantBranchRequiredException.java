package com.mentorship.food_delivery_app.payment.exceptions;

import java.util.UUID;

public class RestaurantBranchRequiredException extends RuntimeException {

    public RestaurantBranchRequiredException(UUID customerId) {
        super("Checkout requires a restaurant branch for customer ID: " + customerId);
    }
}

