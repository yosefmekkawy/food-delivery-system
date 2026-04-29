package com.mentorship.food_delivery_app.cart.controller;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new CartController(cartService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateQuantity_shouldReturnUpdatedCart() throws Exception {
        CartResponseDTO response = new CartResponseDTO(
                1L,
                List.of(new CartItemResponseDTO(10L, "Burger", 3, new BigDecimal("12.50"), new BigDecimal("37.50"), "no onions")),
                new BigDecimal("37.50"),
                "Keep it warm"
        );

        when(cartService.updateItemQuantity(1L, 10L, 3)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/cart/{customerId}/items/{menuItemId}/quantity", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.items[0].menuItemId").value(10L))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.totalAmount").value(37.50));
    }

    @Test
    void updateQuantity_shouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(patch("/api/v1/cart/{customerId}/items/{menuItemId}/quantity", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.quantity").value("Quantity must be at least 1"));
    }

    @Test
    void removeCartItem_shouldReturnNotFoundWhenItemDoesNotExist() throws Exception {
        when(cartService.removeCartItem(1L, 999L)).thenThrow(new CartItemNotFoundException(1L, 999L));

        mockMvc.perform(delete("/api/v1/cart/{customerId}/items/{menuItemId}", 1L, 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Menu item 999 was not found in the cart for customer ID: 1"));
    }

    @Test
    void checkout_shouldReturnCreatedOrder() throws Exception {
        CheckoutCartResponseDTO response = new CheckoutCartResponseDTO(
                new OrderResponseDTO(
                        200L,
                        1L,
                        List.of(),
                        new BigDecimal("25.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("25.00"),
                        "Leave at the gate",
                        "PLACED",
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

        when(cartService.checkoutCart(eq(1L), any(CheckoutCartRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cart/{customerId}/checkout", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckoutCartRequestDTO("Leave at the gate", "CARD"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.order.orderId").value(200L))
                .andExpect(jsonPath("$.order.customerId").value(1L))
                .andExpect(jsonPath("$.order.status").value("PLACED"))
                .andExpect(jsonPath("$.order.totalAmount").value(25.00))
                .andExpect(jsonPath("$.payment.status").value("COMPLETED"))
                .andExpect(jsonPath("$.payment.paymentIntegrationType").value("CARD"));
    }

    @Test
    void checkout_shouldReturnBadRequestWhenPaymentTypeMissing() throws Exception {
        mockMvc.perform(post("/api/v1/cart/{customerId}/checkout", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Leave at the gate\",\"paymentIntegrationType\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.paymentIntegrationType").value("Payment integration type is required"));
    }
}


