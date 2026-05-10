package com.mentorship.food_delivery_app.cart.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mentorship.food_delivery_app.cart.dto.AddToCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CartResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.UpdateCartItemQuantityRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.UpdateCartItemRequestDTO;
import com.mentorship.food_delivery_app.cart.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller class to handle all Cart-related API requests.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Endpoint to view the cart for a specific customer.
     * View the cart for a specific customer.
     * GET /api/v1/cart/{customerId}
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable UUID customerId) {
        return ResponseEntity.ok(cartService.getCartByCustomerId(customerId));
    }

    /**
     * Modify an existing cart item (update quantity and/or note).
     * PUT /api/v1/cart/{customerId}/items/{menuItemId}
     */
    @PutMapping("/{customerId}/items/{menuItemId}")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @PathVariable UUID customerId,
            @PathVariable UUID menuItemId,
            @Valid @RequestBody UpdateCartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.updateCartItem(customerId, menuItemId, request));
    }

    /**
     * Update only the quantity of a specific cart item.
     * PATCH /api/v1/cart/{customerId}/items/{menuItemId}/quantity
     */
    @PatchMapping("/{customerId}/items/{menuItemId}/quantity")
    public ResponseEntity<CartResponseDTO> updateCartItemQuantity(
            @PathVariable UUID customerId,
            @PathVariable UUID menuItemId,
            @Valid @RequestBody UpdateCartItemQuantityRequestDTO request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(customerId, menuItemId, request.getQuantity()));
    }

    /**
     * Remove a specific item from the customer's cart.
     * DELETE /api/v1/cart/{customerId}/items/{menuItemId}
     */
    @DeleteMapping("/{customerId}/items/{menuItemId}")
    public ResponseEntity<CartResponseDTO> removeCartItem(
            @PathVariable UUID customerId,
            @PathVariable UUID menuItemId) {
        return ResponseEntity.ok(cartService.removeCartItem(customerId, menuItemId));
    }

    /**
     * Clear all items from the customer's cart.
     * DELETE /api/v1/cart/{customerId}
     */
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clearCart(@PathVariable UUID customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checkout the current customer cart and create an order snapshot.
     * POST /api/v1/cart/{customerId}/checkout
     */
    @PostMapping("/{customerId}/checkout")
    public ResponseEntity<CheckoutCartResponseDTO> checkoutCart(
            @PathVariable UUID customerId,
            @Valid @RequestBody CheckoutCartRequestDTO request) {
        CheckoutCartResponseDTO checkout = cartService.checkoutCart(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(checkout);
    }

    /**
     * Endpoint to add an item to the customer's cart.
     * POST /api/v1/cart/{customerId}/items
     *
     * Request body example:
     * {
     *   "menuItemId": 3,
     *   "quantity": 2,
     *   "note": "extra spicy"
     * }
     */
    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartResponseDTO> addItem(
            @PathVariable UUID customerId,
            @RequestBody @Valid AddToCartRequestDTO request) {
        CartResponseDTO updatedCart = cartService.addItemToCart(customerId, request);
        return ResponseEntity.ok(updatedCart);
    }
}

