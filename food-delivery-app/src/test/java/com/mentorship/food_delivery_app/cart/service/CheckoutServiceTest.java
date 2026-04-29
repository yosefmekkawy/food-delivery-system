package com.mentorship.food_delivery_app.cart.service;

import com.mentorship.food_delivery_app.cart.dto.CheckoutCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartResponseDTO;
import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.cart.entity.CartItem;
import com.mentorship.food_delivery_app.customer.entity.Customer;
import com.mentorship.food_delivery_app.order.dto.OrderResponseDTO;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.order.service.OrderService;
import com.mentorship.food_delivery_app.payment.dto.PaymentTransactionResponseDTO;
import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;
import com.mentorship.food_delivery_app.payment.exceptions.PaymentConfigurationNotFoundException;
import com.mentorship.food_delivery_app.payment.exceptions.RestaurantBranchRequiredException;
import com.mentorship.food_delivery_app.payment.service.PaymentService;
import com.mentorship.food_delivery_app.restaurant.entity.MenuItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private CheckoutService checkoutService;

    @Test
    void checkout_shouldCreateOrderProcessPaymentAndClearCart() {
        Cart cart = createCart(101L, 1L, 9L, "Gate code is 1234");
        addItem(cart, 11L, "Burger", new BigDecimal("25.00"), 1, "No onions");
        CheckoutCartRequestDTO request = new CheckoutCartRequestDTO("Leave at the gate", "card");

        Order order = new Order();
        order.setId(500L);
        order.setCustomer(cart.getCustomer());
        order.setSubtotal(new BigDecimal("25.00"));
        order.setFee(BigDecimal.ZERO);
        order.setTotal(new BigDecimal("25.00"));
        order.setStatus("PLACED");
        order.setOrderedAt(LocalDateTime.now());
        order.setNote("Leave at the gate");

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setStatus("COMPLETED");
        transaction.setPaymentType("CARD");
        transaction.setAmount(new BigDecimal("25.00"));
        transaction.setTransactionTime(LocalDateTime.now());

        OrderResponseDTO orderResponse = new OrderResponseDTO(
                500L,
                1L,
                new ArrayList<>(),
                new BigDecimal("25.00"),
                BigDecimal.ZERO,
                new BigDecimal("25.00"),
                "Leave at the gate",
                "PLACED",
                order.getOrderedAt()
        );
        PaymentTransactionResponseDTO paymentResponse = new PaymentTransactionResponseDTO(
                transaction.getId(),
                "COMPLETED",
                "CARD",
                new BigDecimal("25.00"),
                transaction.getTransactionTime()
        );

        when(orderService.createOrderFromCart(cart, "Leave at the gate")).thenReturn(order);
        when(paymentService.processPayment(any(PaymentService.PaymentCommand.class))).thenReturn(transaction);
        when(orderService.toResponse(order)).thenReturn(orderResponse);
        when(paymentService.toResponse(transaction)).thenReturn(paymentResponse);

        CheckoutCartResponseDTO response = checkoutService.checkout(cart, request);

        ArgumentCaptor<PaymentService.PaymentCommand> commandCaptor = ArgumentCaptor.forClass(PaymentService.PaymentCommand.class);
        verify(paymentService).processPayment(commandCaptor.capture());
        PaymentService.PaymentCommand command = commandCaptor.getValue();

        assertThat(command.order()).isSameAs(order);
        assertThat(command.customer()).isSameAs(cart.getCustomer());
        assertThat(command.restaurantBranchId()).isEqualTo(9L);
        assertThat(command.paymentIntegrationType()).isEqualTo("card");
        assertThat(command.amount()).isEqualByComparingTo("25.00");
        assertThat(response.getOrder()).isSameAs(orderResponse);
        assertThat(response.getPayment()).isSameAs(paymentResponse);
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getNotes()).isNull();
        assertThat(cart.getRestaurantId()).isNull();
    }

    @Test
    void checkout_shouldNotClearCartWhenPaymentFails() {
        Cart cart = createCart(102L, 1L, 9L, "Keep drinks cold");
        addItem(cart, 21L, "Soda", new BigDecimal("5.00"), 2, null);
        CheckoutCartRequestDTO request = new CheckoutCartRequestDTO("", "CARD");

        Order order = new Order();
        order.setId(600L);
        order.setCustomer(cart.getCustomer());
        order.setTotal(new BigDecimal("10.00"));

        when(orderService.createOrderFromCart(cart, "")).thenReturn(order);
        when(paymentService.processPayment(any(PaymentService.PaymentCommand.class)))
                .thenThrow(new PaymentConfigurationNotFoundException("CARD"));

        assertThatThrownBy(() -> checkoutService.checkout(cart, request))
                .isInstanceOf(PaymentConfigurationNotFoundException.class)
                .hasMessage("Payment integration type 'CARD' is not configured");

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getNotes()).isEqualTo("Keep drinks cold");
        assertThat(cart.getRestaurantId()).isEqualTo(9L);
    }

    @Test
    void checkout_shouldRequireRestaurantBranch() {
        Cart cart = createCart(103L, 1L, null, "Call on arrival");
        addItem(cart, 31L, "Pizza", new BigDecimal("18.00"), 1, null);

        assertThatThrownBy(() -> checkoutService.checkout(cart, new CheckoutCartRequestDTO("Call on arrival", "CARD")))
                .isInstanceOf(RestaurantBranchRequiredException.class)
                .hasMessage("Checkout requires a restaurant branch for customer ID: 1");

        verify(orderService, never()).createOrderFromCart(any(), any());
        verify(paymentService, never()).processPayment(any());
    }

    private Cart createCart(Long cartId, Long customerId, Long restaurantId, String notes) {
        Customer customer = new Customer();
        customer.setId(customerId);

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setCustomer(customer);
        cart.setRestaurantId(restaurantId);
        cart.setNotes(notes);
        cart.setItems(new ArrayList<>());
        cart.setIsLocked(false);
        return cart;
    }

    private void addItem(Cart cart, Long menuItemId, String itemName, BigDecimal price, Integer quantity, String note) {
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
    }
}

