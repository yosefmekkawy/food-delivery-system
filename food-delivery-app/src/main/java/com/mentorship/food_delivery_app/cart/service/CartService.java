package com.mentorship.food_delivery_app.cart.service;

import com.mentorship.food_delivery_app.cart.dto.AddToCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CartItemResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.CartResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.UpdateCartItemRequestDTO;
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
