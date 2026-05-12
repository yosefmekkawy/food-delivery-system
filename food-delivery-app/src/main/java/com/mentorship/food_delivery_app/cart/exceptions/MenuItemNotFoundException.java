package com.mentorship.food_delivery_app.cart.exceptions;

import java.util.UUID;

public class MenuItemNotFoundException extends RuntimeException {

    public MenuItemNotFoundException(UUID menuItemId) {
        super("Menu item not found with ID: " + menuItemId);
    }
}

