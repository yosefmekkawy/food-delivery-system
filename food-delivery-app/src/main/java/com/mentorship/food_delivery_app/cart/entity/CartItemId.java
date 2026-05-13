package com.mentorship.food_delivery_app.cart.entity;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Id class for CartItem.
 * Required for the composite primary key (cart_id, menu_item_id).
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CartItemId implements Serializable {
    private UUID cart;
    private UUID menuItem;
}
