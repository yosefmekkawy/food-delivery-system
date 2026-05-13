package com.mentorship.food_delivery_app.cart.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mentorship.food_delivery_app.restaurant.entity.MenuItem;

/**
 * Repository for MenuItem entity.
 * findById(UUID id) is inherited from JpaRepository — no custom method needed.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
}
