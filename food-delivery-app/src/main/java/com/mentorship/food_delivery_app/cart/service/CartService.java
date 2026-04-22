package com.mentorship.food_delivery_app.cart.service;

import com.mentorship.food_delivery_app.cart.dto.AddToCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CartItemResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.CartResponseDTO;
import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.cart.entity.CartItem;
import com.mentorship.food_delivery_app.cart.repository.CartItemRepository;
import com.mentorship.food_delivery_app.cart.repository.CartRepository;
import com.mentorship.food_delivery_app.cart.repository.MenuItemRepository;
import com.mentorship.food_delivery_app.restaurant.entity.MenuItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Cart operations.
 * This is where the business logic lives (math, mapping, etc.).
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final CartItemRepository cartItemRepository;

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
     * Adds a menu item to the customer's cart.
     * - If the item already exists in the cart → increase its quantity.
     * - If the item is new → create a new CartItem row.
     */
    @Transactional
    public CartResponseDTO addItemToCart(Long customerId, AddToCartRequestDTO request) {
        // 1. Fetch the customer's cart — throw if not found
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found for customer ID: " + customerId));

        // 2. Guard: reject if cart is locked (e.g. order already placed)
        if (Boolean.TRUE.equals(cart.getIsLocked())) {
            throw new RuntimeException("Cart is locked and cannot be modified for customer ID: " + customerId);
        }

        // 3. Fetch the MenuItem — throw if it doesn't exist
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + request.getMenuItemId()));

        // 4. Check if this item is already in the cart
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndMenuItemId(
                cart.getId(), menuItem.getId()
        );

        if (existingItemOpt.isPresent()) {
            // 4a. Item exists → just increase the quantity
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            // 4b. New item → create and add to cart's item list
            CartItem newItem = new CartItem(cart, menuItem, request.getQuantity(), request.getNote());
            cart.getItems().add(newItem);
        }

        // 5. Save the cart — CascadeType.ALL on Cart.items will persist the CartItem automatically
        cartRepository.save(cart);

        // 6. Return the updated cart as a response DTO
        return getCartByCustomerId(customerId);
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
