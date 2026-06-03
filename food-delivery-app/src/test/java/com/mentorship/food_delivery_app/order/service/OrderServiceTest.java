package com.mentorship.food_delivery_app.order.service;

import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.cart.entity.CartItem;
import com.mentorship.food_delivery_app.customer.entity.Customer;
import com.mentorship.food_delivery_app.order.dto.OrderResponseDTO;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.order.entity.OrderItem;
import com.mentorship.food_delivery_app.order.entity.OrderStatus;
import com.mentorship.food_delivery_app.order.entity.OrderTracking;
import com.mentorship.food_delivery_app.order.exceptions.InvalidOrderStateException;
import com.mentorship.food_delivery_app.order.exceptions.OrderNotFoundException;
import com.mentorship.food_delivery_app.order.repository.OrderRepository;
import com.mentorship.food_delivery_app.order.repository.OrderStatusRepository;
import com.mentorship.food_delivery_app.order.repository.OrderTrackingRepository;
import com.mentorship.food_delivery_app.payment.repository.PaymentTransactionRepository;
import com.mentorship.food_delivery_app.restaurant.entity.MenuItem;
import com.mentorship.food_delivery_app.restaurant.entity.RestaurantBranch;
import com.mentorship.food_delivery_app.restaurant.repository.RestaurantBranchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderStatusRepository orderStatusRepository;
    @Mock private OrderTrackingRepository orderTrackingRepository;
    @Mock private RestaurantBranchRepository restaurantBranchRepository;
    @Mock private PaymentTransactionRepository paymentTransactionRepository;

    @InjectMocks
    private OrderService orderService;

    // ─── createOrderFromCart ────────────────────────────────────────────────

    @Test
    void createOrderFromCart_shouldBuildOrderWithCorrectTotals() {
        UUID branchId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        Customer customer = customer(addressId);
        Cart cart = cart(customer, branchId);
        addCartItem(cart, new BigDecimal("10.00"), 2);
        addCartItem(cart, new BigDecimal("5.00"), 1);

        OrderStatus placed = status(1, "PLACED");
        when(orderStatusRepository.findById(1)).thenReturn(Optional.of(placed));
        when(restaurantBranchRepository.findById(branchId)).thenReturn(Optional.of(branchWithId(branchId)));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrderFromCart(cart, "note", null, BigDecimal.ZERO);

        assertThat(order.getSubtotal()).isEqualByComparingTo("25.00");
        assertThat(order.getTotal()).isEqualByComparingTo("25.00");
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getNote()).isEqualTo("note");
        assertThat(order.getRestaurantBranch().getId()).isEqualTo(branchId);
        assertThat(order.getAddressId()).isEqualTo(addressId);
        assertThat(order.getStatus()).isSameAs(placed);
    }

    @Test
    void createOrderFromCart_shouldApplyDiscountToTotal() {
        Customer customer = customer(UUID.randomUUID());
        Cart cart = cart(customer, UUID.randomUUID());
        addCartItem(cart, new BigDecimal("50.00"), 1);

        when(orderStatusRepository.findById(1)).thenReturn(Optional.of(status(1, "PLACED")));
        when(restaurantBranchRepository.findById(any())).thenAnswer(inv -> Optional.of(branchWithId(inv.getArgument(0))));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrderFromCart(cart, null, null, new BigDecimal("10.00"));

        assertThat(order.getSubtotal()).isEqualByComparingTo("50.00");
        assertThat(order.getDiscountValue()).isEqualByComparingTo("10.00");
        assertThat(order.getTotal()).isEqualByComparingTo("40.00");
        assertThat(order.getNote()).isNull();
    }

    @Test
    void createOrderFromCart_shouldNotAllowNegativeTotal() {
        Customer customer = customer(UUID.randomUUID());
        Cart cart = cart(customer, UUID.randomUUID());
        addCartItem(cart, new BigDecimal("5.00"), 1);

        when(orderStatusRepository.findById(1)).thenReturn(Optional.of(status(1, "PLACED")));
        when(restaurantBranchRepository.findById(any())).thenAnswer(inv -> Optional.of(branchWithId(inv.getArgument(0))));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrderFromCart(cart, null, null, new BigDecimal("100.00"));

        assertThat(order.getTotal()).isEqualByComparingTo("0.00");
    }

    // ─── requestReturn ──────────────────────────────────────────────────────

    @Test
    void requestReturn_shouldTransitionFromDeliveredToReturnRequested() {
        Order order = orderWithStatus("DELIVERED");
        OrderStatus returnRequested = status(5, "RETURN_REQUESTED");

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByName("RETURN_REQUESTED")).thenReturn(Optional.of(returnRequested));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDTO response = orderService.requestReturn(order.getId(), "wrong item");

        assertThat(order.getStatus().getName()).isEqualTo("RETURN_REQUESTED");
        verify(orderTrackingRepository).save(any(OrderTracking.class));
    }

    @Test
    void requestReturn_shouldThrowWhenOrderIsNotDelivered() {
        Order order = orderWithStatus("PLACED");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.requestReturn(order.getId(), "reason"))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("DELIVERED");
    }

    @Test
    void requestReturn_shouldThrowWhenOrderNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.requestReturn(id, "reason"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ─── approveReturn ──────────────────────────────────────────────────────

    @Test
    void approveReturn_shouldTransitionFromReturnRequestedToReturned() {
        Order order = orderWithStatus("RETURN_REQUESTED");
        OrderStatus returned = status(6, "RETURNED");

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByName("RETURNED")).thenReturn(Optional.of(returned));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.approveReturn(order.getId());

        assertThat(order.getStatus().getName()).isEqualTo("RETURNED");
        verify(orderTrackingRepository).save(any(OrderTracking.class));
    }

    @Test
    void approveReturn_shouldThrowWhenOrderIsNotReturnRequested() {
        Order order = orderWithStatus("DELIVERED");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.approveReturn(order.getId()))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("RETURN_REQUESTED");
    }

    // ─── cancelByCustomer / rejectByRestaurant ─────────────────��────────────

    @Test
    void cancelByCustomer_shouldTransitionFromPlacedToCancelled() {
        Order order = orderWithStatus("PLACED");
        OrderStatus cancelled = status(7, "CANCELLED");

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByName("CANCELLED")).thenReturn(Optional.of(cancelled));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.cancelByCustomer(order.getId(), "changed my mind");

        assertThat(order.getStatus().getName()).isEqualTo("CANCELLED");
        ArgumentCaptor<OrderTracking> captor = ArgumentCaptor.forClass(OrderTracking.class);
        verify(orderTrackingRepository).save(captor.capture());
        assertThat(captor.getValue().getDescription()).contains("Cancelled by customer");
    }

    @Test
    void rejectByRestaurant_shouldTransitionFromPlacedToCancelled() {
        Order order = orderWithStatus("PLACED");
        OrderStatus cancelled = status(7, "CANCELLED");

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByName("CANCELLED")).thenReturn(Optional.of(cancelled));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.rejectByRestaurant(order.getId(), "out of stock");

        assertThat(order.getStatus().getName()).isEqualTo("CANCELLED");
        ArgumentCaptor<OrderTracking> captor = ArgumentCaptor.forClass(OrderTracking.class);
        verify(orderTrackingRepository).save(captor.capture());
        assertThat(captor.getValue().getDescription()).contains("Rejected by restaurant");
    }

    @Test
    void cancelByCustomer_shouldThrowWhenOrderIsNotPlaced() {
        Order order = orderWithStatus("DELIVERED");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelByCustomer(order.getId(), "reason"))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("PLACED");
    }

    // ─── changeOrderStatus ──────────────────────────────────────────────────

    @Test
    void changeOrderStatus_shouldUpdateStatusAndAppendTracking() {
        Order order = orderWithStatus("PLACED");
        OrderStatus preparing = status(2, "PREPARING");

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderStatusRepository.findById(2)).thenReturn(Optional.of(preparing));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String result = orderService.changeOrderStatus(order.getId(), 2);

        assertThat(order.getStatus().getName()).isEqualTo("PREPARING");
        assertThat(result).contains("PREPARING");
        verify(orderTrackingRepository).save(any(OrderTracking.class));
    }

    @Test
    void changeOrderStatus_shouldThrowWhenOrderNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.changeOrderStatus(id, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── getActiveOrdersForRestaurant ───────────────────────────────────────

    @Test
    void getActiveOrdersForRestaurant_shouldReturnEmptyListWhenNoBranchesFound() {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantBranchRepository.findIdsByRestaurantId(restaurantId)).thenReturn(List.of());

        List<OrderResponseDTO> result = orderService.getActiveOrdersForRestaurant(restaurantId);

        assertThat(result).isEmpty();
        verify(orderRepository, never()).findActiveByBranchIds(any(), any());
    }

    @Test
    void getActiveOrdersForRestaurant_shouldReturnActiveOrders() {
        UUID restaurantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        Order order = orderWithStatus("PLACED");

        when(restaurantBranchRepository.findIdsByRestaurantId(restaurantId)).thenReturn(List.of(branchId));
        when(orderRepository.findActiveByBranchIds(any(), any())).thenReturn(List.of(order));

        List<OrderResponseDTO> result = orderService.getActiveOrdersForRestaurant(restaurantId);

        assertThat(result).hasSize(1);
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private Order orderWithStatus(String statusName) {
        Customer customer = customer(UUID.randomUUID());
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomer(customer);
        order.setSubtotal(new BigDecimal("20.00"));
        order.setFee(BigDecimal.ZERO);
        order.setTotal(new BigDecimal("20.00"));
        order.setDiscountValue(BigDecimal.ZERO);
        order.setAddressId(UUID.randomUUID());
        RestaurantBranch branch = new RestaurantBranch();
        branch.setId(UUID.randomUUID());
        order.setRestaurantBranch(branch);
        order.setOrderedAt(LocalDateTime.now());
        order.setStatus(status(1, statusName));
        order.setItems(new ArrayList<>());
        return order;
    }

    private Customer customer(UUID addressId) {
        Customer c = new Customer();
        c.setId(UUID.randomUUID());
        c.setDefaultAddressId(addressId);
        return c;
    }

    private RestaurantBranch branchWithId(UUID id) {
        RestaurantBranch branch = new RestaurantBranch();
        branch.setId(id);
        return branch;
    }

    private Cart cart(Customer customer, UUID branchId) {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomer(customer);
        cart.setRestaurantId(branchId);
        cart.setItems(new ArrayList<>());
        return cart;
    }

    private void addCartItem(Cart cart, BigDecimal price, int quantity) {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(UUID.randomUUID());
        menuItem.setName("Item");
        menuItem.setPrice(price);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setMenuItem(menuItem);
        item.setQuantity(quantity);
        cart.getItems().add(item);
    }

    private OrderStatus status(int id, String name) {
        return new OrderStatus(id, name, null);
    }
}

