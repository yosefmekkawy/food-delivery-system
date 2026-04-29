package com.mentorship.food_delivery_app.cart.dto;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Payment integration type is required")
    private String paymentIntegrationType;
}

