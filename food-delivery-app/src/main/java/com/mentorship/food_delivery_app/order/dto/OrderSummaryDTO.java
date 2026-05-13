package com.mentorship.food_delivery_app.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
public class OrderSummaryDTO {

    private UUID orderId;
    private LocalDateTime orderedAt;
    private Integer statusId;
    private String statusName;

    private OrderSummaryCustomerDTO customer;

    private UUID restaurantBranchId;
    private UUID deliveryAddressId;

    private List<OrderItemResponseDTO> items;
    private Integer distinctItemCount;
    private Integer totalQuantity;

    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal discountValue;
    private BigDecimal totalAmount;

    private OrderSummaryPaymentDTO payment;

    private String note;
}
