package com.mentorship.food_delivery_app.cart.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import com.mentorship.food_delivery_app.cart.entity.CartItem;
import com.mentorship.food_delivery_app.cart.entity.CartItemId;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
    Optional<CartItem> findByCartIdAndMenuItemId(Long cartId, Long menuItemId);
}
