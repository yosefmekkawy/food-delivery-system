package com.mentorship.food_delivery_app.cart.repository;

import com.mentorship.food_delivery_app.restaurant.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for MenuItem entity.
 * findById(Long id) is inherited from JpaRepository — no custom method needed.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
}
