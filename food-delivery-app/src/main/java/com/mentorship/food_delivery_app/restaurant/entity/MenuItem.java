package com.mentorship.food_delivery_app.restaurant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_item_id")
    private Long id;

    @Column(name = "menu_item_name", nullable = false)
    private String name;

    @Column(name = "menu_item_description")
    private String description;

    @Column(name = "menu_item_price", nullable = false)
    private BigDecimal price;
}
