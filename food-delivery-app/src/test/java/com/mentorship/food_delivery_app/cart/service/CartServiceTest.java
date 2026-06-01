package com.mentorship.food_delivery_app.cart.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.mentorship.food_delivery_app.order.service.CheckoutService;
import com.mentorship.food_delivery_app.restaurant.entity.MenuItem;

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
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID menuItemId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Cart cart = createCart(UUID.fromString("22222222-2222-2222-2222-222222222222"), customerId);
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
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID menuItemId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Cart cart = createCart(UUID.fromString("33333333-3333-3333-3333-333333333333"), customerId);
        CartItem cartItem = addItem(cart, menuItemId, "Pizza", new BigDecimal("20.00"), 1, "extra cheese");
        cart.setRestaurantId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));

        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItemId)).thenReturn(Optional.of(cartItem));

        CartResponseDTO response = cartService.removeCartItem(customerId, menuItemId);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getRestaurantId()).isNull();
    }

    @Test
    void checkoutCart_shouldDelegateToCheckoutService() {
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Cart cart = createCart(UUID.fromString("44444444-4444-4444-4444-444444444444"), customerId);
        addItem(cart, UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"), "Burger", new BigDecimal("10.00"), 2, "no pickles");
        addItem(cart, UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), "Fries", new BigDecimal("5.00"), 1, null);
        cart.setRestaurantId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"));

        CheckoutCartRequestDTO request = new CheckoutCartRequestDTO("Checkout note", "CARD", null);
        CheckoutCartResponseDTO checkoutResponse = new CheckoutCartResponseDTO(
                new OrderResponseDTO(
                    UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                    customerId,
                    List.of(),
                    new BigDecimal("25.00"),
                    BigDecimal.ZERO,
                    new BigDecimal("25.00"),
                    "Checkout note",
                    1,
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

        assertThat(result.getOrder().getOrderId()).isEqualTo(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
        assertThat(result.getPayment().getStatus()).isEqualTo("COMPLETED");
        verify(checkoutService).checkout(cart, request);
        verify(menuItemRepository, never()).findById(any());
    }

    private Cart createCart(UUID cartId, UUID customerId) {
        Customer customer = new Customer();
        customer.setId(customerId);

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setCustomer(customer);
        cart.setItems(new ArrayList<>());
        cart.setIsLocked(false);
        return cart;
    }

    private CartItem addItem(
            Cart cart,
            UUID menuItemId,
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

