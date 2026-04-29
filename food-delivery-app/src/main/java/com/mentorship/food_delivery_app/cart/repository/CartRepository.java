package com.mentorship.food_delivery_app.cart.repository;

import com.mentorship.food_delivery_app.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for Cart entity.
 * Provides methods to interact with the carts table in the database.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find a cart by the customer's ID.
     * Since each customer usually has one active cart, we return an Optional.
     */
    Optional<Cart> findByCustomerId(Long customerId);
}
