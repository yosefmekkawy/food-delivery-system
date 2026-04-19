package com.mentorship.food_delivery_app.cart.service;

import com.mentorship.food_delivery_app.cart.dto.CartItemResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.CartResponseDTO;
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
        // 1. Fetch the cart from the database
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found for customer ID: " + customerId));

        // 2. Map items to DTOs and calculate subtotals
        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                .map(this::mapToItemDTO)
                .collect(Collectors.toList());

        // 3. Calculate grand total
        BigDecimal grandTotal = itemDTOs.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Build and return the final response
        return new CartResponseDTO(
                cart.getId(),
                itemDTOs,
                grandTotal,
                cart.getNotes()
        );
    }

    /**
     * Helper method to convert a CartItem entity to a DTO and calculate its subtotal.
     */
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
