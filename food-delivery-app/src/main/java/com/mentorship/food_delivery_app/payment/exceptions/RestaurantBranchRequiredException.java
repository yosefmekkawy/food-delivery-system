package com.mentorship.food_delivery_app.payment.exceptions;

public class RestaurantBranchRequiredException extends RuntimeException {

    public RestaurantBranchRequiredException(Long customerId) {
        super("Checkout requires a restaurant branch for customer ID: " + customerId);
    }
}

