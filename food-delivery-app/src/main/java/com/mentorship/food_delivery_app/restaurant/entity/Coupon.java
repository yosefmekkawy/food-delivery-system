package com.mentorship.food_delivery_app.restaurant.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "coupon_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "coupon_restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "coupon_amount", precision = 6, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_discount_type", nullable = false, length = 15)
    private CouponDiscountType discountType;

    @Column(name = "coupon_min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "coupon_available_from", nullable = false)
    private LocalDateTime availableFrom;

    @Column(name = "coupon_available_to", nullable = false)
    private LocalDateTime availableTo;

    @Column(name = "coupon_is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "coupon_created_at")
    private LocalDateTime createdAt;

    @Column(name = "coupon_last_modified")
    private LocalDateTime lastModified;
}


