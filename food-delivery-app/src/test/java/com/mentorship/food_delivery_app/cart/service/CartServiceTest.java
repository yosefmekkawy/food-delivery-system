package com.mentorship.food_delivery_app.cart.service;

import com.mentorship.food_delivery_app.cart.dto.CartResponseDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartResponseDTO;
import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.cart.entity.CartItem;
import com.mentorship.food_delivery_app.cart.repository.CartItemRepository;
import com.mentorship.food_delivery_app.cart.repository.CartRepository;
import com.mentorship.food_delivery_app.cart.repository.MenuItemRepository;
import com.mentorship.food_delivery_app.customer.entity.Customer;
import com.mentorship.food_delivery_app.order.dto.OrderResponseDTO;
import com.mentorship.food_delivery_app.payment.dto.PaymentTransactionResponseDTO;
import com.mentorship.food_delivery_app.restaurant.entity.MenuItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CheckoutService checkoutService;

    @InjectMocks
    private CartService cartService;

    @Test
    void updateItemQuantity_shouldUpdateQuantityAndRecalculateTotal() {
        Long customerId = 1L;
        Long menuItemId = 10L;
        Cart cart = createCart(100L, customerId, "Keep it warm");
        CartItem cartItem = addItem(cart, menuItemId, "Burger", new BigDecimal("12.50"), 2, "no onions");

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItemId)).thenReturn(Optional.of(cartItem));

        CartResponseDTO response = cartService.updateItemQuantity(customerId, menuItemId, 4);

        assertThat(cartItem.getQuantity()).isEqualTo(4);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getQuantity()).isEqualTo(4);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void removeCartItem_shouldRemoveItemAndResetMetadataWhenCartBecomesEmpty() {
        Long customerId = 1L;
        Long menuItemId = 10L;
        Cart cart = createCart(101L, customerId, "Leave at the gate");
        CartItem cartItem = addItem(cart, menuItemId, "Pizza", new BigDecimal("20.00"), 1, "extra cheese");
        cart.setRestaurantId(8L);

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItemId)).thenReturn(Optional.of(cartItem));

        CartResponseDTO response = cartService.removeCartItem(customerId, menuItemId);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getRestaurantId()).isNull();
        assertThat(cart.getNotes()).isNull();
    }

    @Test
    void checkoutCart_shouldDelegateToCheckoutService() {
        Long customerId = 1L;
        Cart cart = createCart(102L, customerId, "Original note");
        addItem(cart, 11L, "Burger", new BigDecimal("10.00"), 2, "no pickles");
        addItem(cart, 12L, "Fries", new BigDecimal("5.00"), 1, null);
        cart.setRestaurantId(12L);

        CheckoutCartRequestDTO request = new CheckoutCartRequestDTO("Checkout note", "CARD");
        CheckoutCartResponseDTO checkoutResponse = new CheckoutCartResponseDTO(
                new OrderResponseDTO(
                        500L,
                        customerId,
                        List.of(),
                        new BigDecimal("25.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("25.00"),
                        "Checkout note",
                        "PLACED",
                        LocalDateTime.now()
                ),
                new PaymentTransactionResponseDTO(
                        java.util.UUID.randomUUID(),
                        "COMPLETED",
                        "CARD",
                        new BigDecimal("25.00"),
                        LocalDateTime.now()
                )
        );

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.existsByCartId(cart.getId())).thenReturn(true);
        when(checkoutService.checkout(cart, request)).thenReturn(checkoutResponse);

        CheckoutCartResponseDTO result = cartService.checkoutCart(customerId, request);

        assertThat(result.getOrder().getOrderId()).isEqualTo(500L);
        assertThat(result.getPayment().getStatus()).isEqualTo("COMPLETED");
        verify(checkoutService).checkout(cart, request);
        verify(menuItemRepository, never()).findById(any());
    }

    private Cart createCart(Long cartId, Long customerId, String notes) {
        Customer customer = new Customer();
        customer.setId(customerId);

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setCustomer(customer);
        cart.setNotes(notes);
        cart.setItems(new ArrayList<>());
        cart.setIsLocked(false);
        return cart;
    }

    private CartItem addItem(
            Cart cart,
            Long menuItemId,
            String itemName,
            BigDecimal price,
            Integer quantity,
            String note) {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(menuItemId);
        menuItem.setName(itemName);
        menuItem.setPrice(price);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setMenuItem(menuItem);
        item.setQuantity(quantity);
        item.setNote(note);
        cart.getItems().add(item);
        return item;
    }
}

