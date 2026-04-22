package com.mentorship.food_delivery_app.cart.exceptions;

public class MenuItemNotFoundException extends RuntimeException {

    public MenuItemNotFoundException(Long menuItemId) {
        super("Menu item not found with ID: " + menuItemId);
    }
}

