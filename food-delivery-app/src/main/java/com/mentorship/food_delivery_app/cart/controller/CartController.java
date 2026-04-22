package com.mentorship.food_delivery_app.cart.controller;

import com.mentorship.food_delivery_app.cart.dto.AddToCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CartResponseDTO;
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
     * GET /api/v1/cart/{customerId}
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable Long customerId) {
        CartResponseDTO cartResponse = cartService.getCartByCustomerId(customerId);
        return ResponseEntity.ok(cartResponse);
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

