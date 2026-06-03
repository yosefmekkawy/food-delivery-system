package com.mentorship.food_delivery_app.customer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderResponse {
    private UUID orderId;
    private String status;
    private Instant orderDate;
    private String restaurantName;
    private BigDecimal total;
    private List<CustomerOrderItemResponse> items;

}
