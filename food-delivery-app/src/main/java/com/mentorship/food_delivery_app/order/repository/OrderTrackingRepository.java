package com.mentorship.food_delivery_app.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mentorship.food_delivery_app.order.entity.OrderTracking;

@Repository
public interface OrderTrackingRepository extends JpaRepository<OrderTracking, UUID> {
	Optional<OrderTracking> findByOrder_Id(UUID orderId);
}
