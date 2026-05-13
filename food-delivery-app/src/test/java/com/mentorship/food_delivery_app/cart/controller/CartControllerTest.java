package com.mentorship.food_delivery_app.cart.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorship.food_delivery_app.cart.dto.CartItemResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.CartResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartResponseDTO;
import com.mentorship.food_delivery_app.cart.exceptions.CartItemNotFoundException;
import com.mentorship.food_delivery_app.cart.service.CartService;
import com.mentorship.food_delivery_app.common.exceptions.GlobalExceptionHandler;
import com.mentorship.food_delivery_app.order.dto.OrderResponseDTO;
import com.mentorship.food_delivery_app.payment.dto.PaymentTransactionResponseDTO;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

        @BeforeEach
        @SuppressWarnings("unused")
        void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new CartController(cartService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateQuantity_shouldReturnUpdatedCart() throws Exception {
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID menuItemId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        CartResponseDTO response = new CartResponseDTO(
                customerId,
                List.of(new CartItemResponseDTO(menuItemId, "Burger", 3, new BigDecimal("12.50"), new BigDecimal("37.50"), "no onions")),
                new BigDecimal("37.50")
        );

        when(cartService.updateItemQuantity(customerId, menuItemId, 3)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/cart/{customerId}/items/{menuItemId}/quantity", customerId, menuItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(customerId.toString()))
                .andExpect(jsonPath("$.items[0].menuItemId").value(menuItemId.toString()))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.totalAmount").value(37.50));
    }

    @Test
    void updateQuantity_shouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(patch("/api/v1/cart/{customerId}/items/{menuItemId}/quantity", UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.quantity").value("Quantity must be at least 1"));
    }

    @Test
    void removeCartItem_shouldReturnNotFoundWhenItemDoesNotExist() throws Exception {
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID menuItemId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        when(cartService.removeCartItem(customerId, menuItemId)).thenThrow(new CartItemNotFoundException(customerId, menuItemId));

        mockMvc.perform(delete("/api/v1/cart/{customerId}/items/{menuItemId}", customerId, menuItemId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Menu item " + menuItemId + " was not found in the cart for customer ID: " + customerId));
    }

    @Test
    void checkout_shouldReturnCreatedOrder() throws Exception {
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID orderId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        CheckoutCartResponseDTO response = new CheckoutCartResponseDTO(
                new OrderResponseDTO(
                        orderId,
                        customerId,
                        List.of(),
                        new BigDecimal("25.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("25.00"),
                        "Leave at the gate",
                        1,
                        null
                ),
                new PaymentTransactionResponseDTO(
                        java.util.UUID.randomUUID(),
                        "COMPLETED",
                        "CARD",
                        new BigDecimal("25.00"),
                        null
                )
        );

        when(cartService.checkoutCart(eq(customerId), any(CheckoutCartRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cart/{customerId}/checkout", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckoutCartRequestDTO("Leave at the gate", "CARD"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.order.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.order.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.order.statusId").value(1))
                .andExpect(jsonPath("$.order.totalAmount").value(25.00))
                .andExpect(jsonPath("$.payment.status").value("COMPLETED"))
                .andExpect(jsonPath("$.payment.paymentIntegrationType").value("CARD"));
    }

    @Test
    void checkout_shouldReturnBadRequestWhenPaymentTypeMissing() throws Exception {
        mockMvc.perform(post("/api/v1/cart/{customerId}/checkout", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Leave at the gate\",\"paymentIntegrationType\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.paymentIntegrationType").value("Payment integration type is required"));
    }
}


