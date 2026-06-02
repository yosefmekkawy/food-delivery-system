package com.mentorship.food_delivery_app.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPreferredPaymentResponseDTO {

    private Long preferredPaymentId;
    private String paymentIntegrationType;
}
