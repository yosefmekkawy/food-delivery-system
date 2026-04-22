package com.mentorship.food_delivery_app.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating only the quantity of an item already present in the cart.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemQuantityRequestDTO {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}

