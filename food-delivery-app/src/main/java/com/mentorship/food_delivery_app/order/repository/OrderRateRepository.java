package com.mentorship.food_delivery_app.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mentorship.food_delivery_app.order.entity.OrderRate;

@Repository
public interface OrderRateRepository extends JpaRepository<OrderRate, UUID> {

    boolean existsByOrder_Id(UUID orderId);
}
