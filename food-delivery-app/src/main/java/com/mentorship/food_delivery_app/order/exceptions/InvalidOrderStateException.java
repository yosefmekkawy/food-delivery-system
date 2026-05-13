package com.mentorship.food_delivery_app.order.exceptions;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
