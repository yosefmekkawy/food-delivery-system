package com.mentorship.food_delivery_app.cart.service;

import com.mentorship.food_delivery_app.cart.dto.CartItemResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.CartResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.UpdateCartItemRequestDTO;
import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.cart.entity.CartItem;
import com.mentorship.food_delivery_app.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Cart operations.
 * This is where the business logic lives (math, mapping, etc.).
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    /**
     * Retrieves the cart for a user and calculates all totals.
     */
    @Transactional(readOnly = true)
    public CartResponseDTO getCartByCustomerId(Long customerId) {
        Cart cart = findCartByCustomerId(customerId);
        return buildCartResponse(cart);
    }

    /**
     * Updates the quantity and/or note of an existing item in the customer's cart.
     */
    @Transactional
    public CartResponseDTO updateCartItem(Long customerId, Long menuItemId, UpdateCartItemRequestDTO request) {
        Cart cart = findCartByCustomerId(customerId);

        if (Boolean.TRUE.equals(cart.getIsLocked())) {
            throw new RuntimeException("Cart is locked and cannot be modified");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getMenuItem().getId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Menu item " + menuItemId + " is not in the cart for customer " + customerId));

        item.setQuantity(request.getQuantity());
        item.setNote(request.getNote());

        return buildCartResponse(cart);
    }

    /**
     * Clears all items (and resets restaurant / notes) from the customer's cart.
     * The cart record itself is kept so the customer can keep using it.
     */
    @Transactional
    public void clearCart(Long customerId) {
        Cart cart = findCartByCustomerId(customerId);

        if (Boolean.TRUE.equals(cart.getIsLocked())) {
            throw new RuntimeException("Cart is locked and cannot be cleared");
        }

        cart.getItems().clear();
        cart.setRestaurantId(null);
        cart.setNotes(null);
    }

    private Cart findCartByCustomerId(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found for customer ID: " + customerId));
    }

    private CartResponseDTO buildCartResponse(Cart cart) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                .map(this::mapToItemDTO)
                .collect(Collectors.toList());

        BigDecimal grandTotal = itemDTOs.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(
                cart.getId(),
                itemDTOs,
                grandTotal,
                cart.getNotes()
        );
    }

    private CartItemResponseDTO mapToItemDTO(CartItem item) {
        BigDecimal unitPrice = item.getMenuItem().getPrice();
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(item.getQuantity()));

        return new CartItemResponseDTO(
                item.getMenuItem().getName(),
                item.getQuantity(),
                unitPrice,
                subtotal,
                item.getNote()
        );
    }
}
