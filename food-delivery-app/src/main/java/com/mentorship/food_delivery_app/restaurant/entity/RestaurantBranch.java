package com.mentorship.food_delivery_app.restaurant.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "restaurant_branch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantBranch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "branch_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_rest_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "branch_delivery_fee", precision = 6, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "branch_min_order", precision = 6, scale = 2)
    private BigDecimal minOrder;

    @Column(name = "branch_city", nullable = false, length = 100)
    private String city;

    @Column(name = "branch_open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "branch_close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "branch_phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "branch_estimated_delivery_time")
    private Integer estimatedDeliveryTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "modified_by")
    private UUID modifiedBy;

    @Column(name = "admin_id")
    private UUID adminId;
}
