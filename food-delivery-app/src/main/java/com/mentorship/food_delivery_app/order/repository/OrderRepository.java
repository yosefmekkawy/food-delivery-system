package com.mentorship.food_delivery_app.order.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mentorship.food_delivery_app.order.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

	@Query("select o from Order o join o.status s where o.restaurantBranchId in :branchIds and s.name not in :excludedStatuses")
	List<Order> findActiveByBranchIds(@Param("branchIds") List<UUID> branchIds,
									  @Param("excludedStatuses") List<String> excludedStatuses);
}

