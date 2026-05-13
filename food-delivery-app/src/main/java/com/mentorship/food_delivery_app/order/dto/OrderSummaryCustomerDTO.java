package com.mentorship.food_delivery_app.order.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryCustomerDTO {

    private UUID customerId;
    private String fullName;
    private String phone;
    private String email;
}
