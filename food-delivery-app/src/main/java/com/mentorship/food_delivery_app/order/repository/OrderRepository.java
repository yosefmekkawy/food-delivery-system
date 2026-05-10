package com.mentorship.food_delivery_app.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mentorship.food_delivery_app.order.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
}

