package com.mentorship.food_delivery_app.customer.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderItemResponse {
    private String name;
    private int quantity;
    private BigDecimal unitPrice;
}
