package com.mentorship.food_delivery_app.cart.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Id class for CartItem.
 * Required for the composite primary key (cart_id, menu_item_id).
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CartItemId implements Serializable {
    private Long cart;
    private Long menuItem;
}
