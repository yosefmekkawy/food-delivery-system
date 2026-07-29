package com.mentorship.food_delivery_app.order.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.mentorship.food_delivery_app.restaurant.entity.MenuItem;

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
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "order_item_unit_price", nullable = false, precision = 9, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "order_item_quantity", nullable = false)
    private Integer quantity;

    @Column(name = "order_item_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "order_item_note")
    private String note;
}

