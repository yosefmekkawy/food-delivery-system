package com.mentorship.food_delivery_app.order.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_rate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_rate_id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_rate_order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "order_rate_rating", nullable = false)
    private Integer rating;

    @Column(name = "order_rate_comment", length = 500)
    private String comment;

    @Column(name = "order_rate_created_at", nullable = false)
    private LocalDateTime createdAt;
}
