package com.mentorship.food_delivery_app.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for mirroring the full cart to the user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private UUID cartId;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalAmount;
}
