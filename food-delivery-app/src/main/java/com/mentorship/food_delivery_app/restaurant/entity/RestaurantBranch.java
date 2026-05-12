package com.mentorship.food_delivery_app.restaurant.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "branch_rest_id", nullable = false)
    private UUID restaurantId;
}
