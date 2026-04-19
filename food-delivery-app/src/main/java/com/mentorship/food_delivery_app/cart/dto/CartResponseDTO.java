package com.mentorship.food_delivery_app.cart.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for mirroring the full cart to the user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private Long cartId;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalAmount;
    private String notes;
}
