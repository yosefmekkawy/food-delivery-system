package com.mentorship.food_delivery_app.order.dto;

import java.time.LocalDateTime;
import java.util.UUID;

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
public class OrderRateResponseDTO {

    private UUID orderId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
