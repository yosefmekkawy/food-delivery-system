package com.mentorship.food_delivery_app.order.service;

import com.mentorship.food_delivery_app.cart.checkout.CheckoutContext;
import com.mentorship.food_delivery_app.cart.checkout.CheckoutHandler;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartResponseDTO;
import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.customer.entity.Customer;
import com.mentorship.food_delivery_app.order.dto.OrderResponseDTO;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.order.entity.OrderStatus;
import com.mentorship.food_delivery_app.payment.dto.PaymentTransactionResponseDTO;
import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;
import com.mentorship.food_delivery_app.payment.service.PaymentService;
import com.mentorship.food_delivery_app.restaurant.entity.RestaurantBranch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CheckoutHandler checkoutChain;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private CheckoutService checkoutService;

    @Test
    void checkout_shouldRunChainAndBuildResponse() {
        UUID customerId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Customer customer = new Customer();
        customer.setId(customerId);

        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomer(customer);
        cart.setRestaurantId(branchId);
        cart.setItems(new ArrayList<>());
        cart.setIsLocked(false);

        CheckoutCartRequestDTO request = new CheckoutCartRequestDTO("note", "CARD", null);

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomer(customer);
        order.setSubtotal(new BigDecimal("25.00"));
        order.setFee(BigDecimal.ZERO);
        order.setTotal(new BigDecimal("25.00"));
        order.setStatus(new OrderStatus(1, "PLACED", null));
        order.setOrderedAt(LocalDateTime.now());
        RestaurantBranch branch = new RestaurantBranch();
        branch.setId(branchId);
        order.setRestaurantBranch(branch);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setStatus("COMPLETED");
        transaction.setPaymentType("CARD");
        transaction.setAmount(new BigDecimal("25.00"));
        transaction.setTransactionTime(LocalDateTime.now());

        OrderResponseDTO orderResponse = new OrderResponseDTO(
                order.getId(), customerId, new ArrayList<>(),
                new BigDecimal("25.00"), BigDecimal.ZERO, new BigDecimal("25.00"),
                "note", 1, order.getOrderedAt()
        );
        PaymentTransactionResponseDTO paymentResponse = new PaymentTransactionResponseDTO(
                transaction.getId(), "COMPLETED", "CARD",
                new BigDecimal("25.00"), transaction.getTransactionTime()
        );

        doAnswer(invocation -> {
            CheckoutContext ctx = invocation.getArgument(0);
            ctx.setOrder(order);
            ctx.setPaymentTransaction(transaction);
            return null;
        }).when(checkoutChain).handle(any(CheckoutContext.class));

        when(orderService.toResponse(order)).thenReturn(orderResponse);
        when(paymentService.toResponse(transaction)).thenReturn(paymentResponse);

        CheckoutCartResponseDTO response = checkoutService.checkout(cart, request);

        verify(checkoutChain).handle(any(CheckoutContext.class));
        assertThat(response.getOrder()).isSameAs(orderResponse);
        assertThat(response.getPayment()).isSameAs(paymentResponse);
    }

    @Test
    void checkout_shouldPropagateChainException() {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        cart.setCustomer(customer);
        cart.setItems(new ArrayList<>());

        doThrow(new RuntimeException("chain error")).when(checkoutChain).handle(any());

        assertThatThrownBy(() -> checkoutService.checkout(cart, new CheckoutCartRequestDTO("", "CARD", null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("chain error");
    }
}

