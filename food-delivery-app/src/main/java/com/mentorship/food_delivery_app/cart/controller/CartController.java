package com.mentorship.food_delivery_app.cart.controller;

import com.mentorship.food_delivery_app.cart.dto.AddToCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CartResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.UpdateCartItemRequestDTO;
import com.mentorship.food_delivery_app.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.getCartByCustomerId(customerId));
    }

    /**
     * Modify an existing cart item (update quantity and/or note).
     * PUT /api/v1/cart/{customerId}/items/{menuItemId}
     */
    @PutMapping("/{customerId}/items/{menuItemId}")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @PathVariable Long customerId,
            @PathVariable Long menuItemId,
            @Valid @RequestBody UpdateCartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.updateCartItem(customerId, menuItemId, request));
    }

    /**
     * Clear all items from the customer's cart.
     * DELETE /api/v1/cart/{customerId}
     */
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
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
            @PathVariable Long customerId,
            @RequestBody @Valid AddToCartRequestDTO request) {
        CartResponseDTO updatedCart = cartService.addItemToCart(customerId, request);
        return ResponseEntity.ok(updatedCart);
    }
}

