package com.mentorship.food_delivery_app.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for checking out the customer's active cart.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutCartRequestDTO {

    private String note;
}

