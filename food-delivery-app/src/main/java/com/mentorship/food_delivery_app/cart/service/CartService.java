package com.mentorship.food_delivery_app.cart.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mentorship.food_delivery_app.cart.dto.AddToCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CartItemResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.CartResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.UpdateCartItemRequestDTO;
import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.cart.entity.CartItem;
import com.mentorship.food_delivery_app.cart.exceptions.CartItemNotFoundException;
import com.mentorship.food_delivery_app.cart.exceptions.CartLockedException;
import com.mentorship.food_delivery_app.cart.exceptions.CartNotFoundException;
import com.mentorship.food_delivery_app.cart.exceptions.EmptyCartException;
import com.mentorship.food_delivery_app.cart.exceptions.MenuItemNotFoundException;
import com.mentorship.food_delivery_app.cart.repository.CartItemRepository;
import com.mentorship.food_delivery_app.cart.repository.CartRepository;
import com.mentorship.food_delivery_app.cart.repository.MenuItemRepository;
import com.mentorship.food_delivery_app.restaurant.entity.MenuItem;

import lombok.RequiredArgsConstructor;

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
    private final CheckoutService checkoutService;

    /**
     * Retrieves the cart for a user and calculates all totals.
     */
    @Transactional(readOnly = true)
    public CartResponseDTO getCartByCustomerId(UUID customerId) {
        Cart cart = findCartByCustomerId(customerId);
        return buildCartResponse(cart);
    }

    /**
     * Updates the quantity and/or note of an existing item in the customer's cart.
     */
    @Transactional
    public CartResponseDTO updateCartItem(UUID customerId, UUID menuItemId, UpdateCartItemRequestDTO request) {
        Cart cart = findCartByCustomerId(customerId);
        validateCartIsMutable(cart, "updated");
        CartItem item = findCartItem(cart, customerId, menuItemId);

        item.setQuantity(request.getQuantity());
        item.setNote(request.getNote());

        return buildCartResponse(cart);
    }

    /**
     * Updates the quantity only for a cart item.
     */
    @Transactional
    public CartResponseDTO updateItemQuantity(UUID customerId, UUID menuItemId, Integer quantity) {
        Cart cart = findCartByCustomerId(customerId);
        validateCartIsMutable(cart, "updated");

        CartItem item = findCartItem(cart, customerId, menuItemId);
        item.setQuantity(quantity);

        return buildCartResponse(cart);
    }

    /**
     * Removes a single item from the customer's cart.
     */
    @Transactional
    public CartResponseDTO removeCartItem(UUID customerId, UUID menuItemId) {
        Cart cart = findCartByCustomerId(customerId);
        validateCartIsMutable(cart, "modified");

        CartItem item = findCartItem(cart, customerId, menuItemId);
        cart.removeItem(item);
        resetCartMetadataIfEmpty(cart);

        return buildCartResponse(cart);
    }

    /**
     * Converts the customer's cart into an order snapshot and clears the active cart.
     */
    @Transactional
    public CheckoutCartResponseDTO checkoutCart(UUID customerId, CheckoutCartRequestDTO request) {
        Cart cart = findCartByCustomerId(customerId);
        validateCartIsMutable(cart, "checked out");
        ensureCartHasItems(customerId, cart);
        return checkoutService.checkout(cart, request);
    }

    /**
     * Clears all items (and resets restaurant) from the customer's cart.
     * The cart record itself is kept so the customer can keep using it.
     */
    @Transactional
    public void clearCart(UUID customerId) {
        Cart cart = findCartByCustomerId(customerId);
        validateCartIsMutable(cart, "cleared");
        cart.clearItems();
        resetCartMetadataIfEmpty(cart);
    }

    private Cart findCartByCustomerId(UUID customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartNotFoundException(customerId));
    }

    private CartResponseDTO buildCartResponse(Cart cart) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getMenuItem().getId()))
                .map(this::mapToItemDTO)
                .toList();

        BigDecimal grandTotal = itemDTOs.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(
            cart.getId(),
            itemDTOs,
            grandTotal
        );
    }

    /**
     * Adds a menu item to the customer's cart.
     * - If the item already exists in the cart → increase its quantity.
     * - If the item is new → create a new CartItem row.
     */
    @Transactional
    public CartResponseDTO addItemToCart(UUID customerId, AddToCartRequestDTO request) {
        Cart cart = findCartByCustomerId(customerId);
        validateCartIsMutable(cart, "modified");

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new MenuItemNotFoundException(request.getMenuItemId()));

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndMenuItemId(
                cart.getId(), menuItem.getId()
        );

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            if (StringUtils.hasText(request.getNote())) {
                existingItem.setNote(request.getNote());
            }
        } else {
            CartItem newItem = new CartItem(null, cart, menuItem, request.getQuantity(), request.getNote());
            cart.addItem(newItem);
        }

        cartRepository.save(cart);
        return getCartByCustomerId(customerId);
    }

    /**
     * Helper method to convert a CartItem entity to a DTO and calculate its subtotal.
     */
    private CartItemResponseDTO mapToItemDTO(CartItem item) {
        BigDecimal unitPrice = item.getMenuItem().getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponseDTO(
                item.getMenuItem().getId(),
                item.getMenuItem().getName(),
                item.getQuantity(),
                unitPrice,
                subtotal,
                item.getNote()
        );
    }

    private void validateCartIsMutable(Cart cart, String action) {
        if (Boolean.TRUE.equals(cart.getIsLocked())) {
            throw new CartLockedException(cart.getId(), action);
        }
    }

    private CartItem findCartItem(Cart cart, UUID customerId, UUID menuItemId) {
        return cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItemId)
                .orElseThrow(() -> new CartItemNotFoundException(customerId, menuItemId));
    }

    private void ensureCartHasItems(UUID customerId, Cart cart) {
        if (!cartItemRepository.existsByCartId(cart.getId())) {
            throw new EmptyCartException(customerId);
        }
    }

    private void resetCartMetadataIfEmpty(Cart cart) {
        if (cart.getItems().isEmpty()) {
            cart.setRestaurantId(null);
        }
    }
}
