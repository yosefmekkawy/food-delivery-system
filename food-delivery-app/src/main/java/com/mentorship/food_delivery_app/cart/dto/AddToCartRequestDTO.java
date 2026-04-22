package com.mentorship.food_delivery_app.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequestDTO {

    @NotNull(message = "menuItemId is required")
    @Min(value = 1, message = "menuItemId must be a positive number")
    private Long menuItemId;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    // note is optional — no constraint needed
    private String note;
}
