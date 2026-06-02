package com.mentorship.food_delivery_app.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.cart.entity.CartItem;
import com.mentorship.food_delivery_app.customer.entity.Customer;
import com.mentorship.food_delivery_app.order.dto.OrderItemResponseDTO;
import com.mentorship.food_delivery_app.order.dto.OrderResponseDTO;
import com.mentorship.food_delivery_app.order.dto.OrderSummaryCustomerDTO;
import com.mentorship.food_delivery_app.order.dto.OrderSummaryDTO;
import com.mentorship.food_delivery_app.order.dto.OrderSummaryPaymentDTO;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.order.entity.OrderItem;
import com.mentorship.food_delivery_app.order.entity.OrderStatus;
import com.mentorship.food_delivery_app.order.entity.OrderTracking;
import com.mentorship.food_delivery_app.order.exceptions.InvalidOrderStateException;
import com.mentorship.food_delivery_app.order.exceptions.OrderNotFoundException;
import com.mentorship.food_delivery_app.order.repository.OrderRepository;
import com.mentorship.food_delivery_app.order.repository.OrderStatusRepository;
import com.mentorship.food_delivery_app.order.repository.OrderTrackingRepository;
import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;
import com.mentorship.food_delivery_app.payment.repository.PaymentTransactionRepository;
import com.mentorship.food_delivery_app.restaurant.entity.RestaurantBranch;
import com.mentorship.food_delivery_app.restaurant.repository.RestaurantBranchRepository;
import com.mentorship.food_delivery_app.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal DELIVERY_FEE = BigDecimal.ZERO;
    private static final int DEFAULT_ORDER_STATUS_ID = 1;
        private static final String STATUS_PLACED = "PLACED";
        private static final String STATUS_DELIVERED = "DELIVERED";
        private static final String STATUS_RETURN_REQUESTED = "RETURN_REQUESTED";
        private static final String STATUS_RETURNED = "RETURNED";
        private static final String STATUS_CANCELLED = "CANCELLED";
        private static final List<String> FINAL_STATUSES = List.of(
                        "FAILED",
                        STATUS_RETURNED,
                        STATUS_DELIVERED,
                        STATUS_CANCELLED
        );

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderTrackingRepository orderTrackingRepository;
        private final RestaurantBranchRepository restaurantBranchRepository;
        private final PaymentTransactionRepository paymentTransactionRepository;

    @Transactional
    public Order createOrderFromCart(Cart cart, String checkoutNote) {
        return createOrderFromCart(cart, checkoutNote, null, BigDecimal.ZERO);
    }

    @Transactional
    public Order createOrderFromCart(Cart cart, String checkoutNote, UUID couponId, BigDecimal discountValue) {
        List<OrderItem> orderItems = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getMenuItem().getId()))
                .map(this::mapToOrderItem)
                .toList();

        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal resolvedDiscount = discountValue != null ? discountValue : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(DELIVERY_FEE).subtract(resolvedDiscount).max(BigDecimal.ZERO);

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setSubtotal(subtotal);
        order.setFee(DELIVERY_FEE);
        order.setDiscountValue(resolvedDiscount);
        order.setTotal(total);
        order.setOrderedAt(LocalDateTime.now());
        order.setStatus(getStatusOrThrow(DEFAULT_ORDER_STATUS_ID));
        order.setAddressId(cart.getCustomer().getDefaultAddressId());
        order.setNote(StringUtils.hasText(checkoutNote) ? checkoutNote : null);
        order.setRestaurantBranch(getRestaurantBranchOrThrow(cart.getRestaurantId()));
        order.setCouponId(couponId);
        orderItems.forEach(order::addItem);

        return orderRepository.save(order);
    }

    private OrderItem mapToOrderItem(CartItem cartItem) {
        BigDecimal unitPrice = cartItem.getMenuItem().getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new OrderItem(
                null,
                null,
                cartItem.getMenuItem(),
                unitPrice,
                cartItem.getQuantity(),
                subtotal,
                cartItem.getNote()
        );
    }

    public OrderResponseDTO toResponse(Order order) {
        List<OrderItemResponseDTO> items = order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getMenuItem().getId()))
                .map(item -> new OrderItemResponseDTO(
                        item.getMenuItem().getId(),
                        item.getMenuItem().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal(),
                        item.getNote()
                ))
                .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getCustomer().getId(),
                items,
                order.getSubtotal(),
                order.getFee(),
                order.getTotal(),
                order.getNote(),
                order.getStatus().getId(),
                order.getOrderedAt()
                );
        }
    
        public OrderResponseDTO getOrderById(UUID orderId){
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
                return toResponse(order);
        }

        @Transactional(readOnly = true)
        public OrderSummaryDTO getOrderSummary(UUID orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new OrderNotFoundException(orderId));

                List<OrderItemResponseDTO> items = order.getItems().stream()
                                .sorted(Comparator.comparing(item -> item.getMenuItem().getId()))
                                .map(item -> new OrderItemResponseDTO(
                                                item.getMenuItem().getId(),
                                                item.getMenuItem().getName(),
                                                item.getQuantity(),
                                                item.getUnitPrice(),
                                                item.getSubtotal(),
                                                item.getNote()))
                                .toList();

                int totalQuantity = items.stream()
                                .mapToInt(OrderItemResponseDTO::getQuantity)
                                .sum();

                OrderSummaryPaymentDTO paymentDto = paymentTransactionRepository
                                .findTopByOrder_IdOrderByTransactionTimeDesc(orderId)
                                .map(this::toPaymentSummary)
                                .orElse(null);

                return OrderSummaryDTO.builder()
                                .orderId(order.getId())
                                .orderedAt(order.getOrderedAt())
                                .statusId(order.getStatus().getId())
                                .statusName(order.getStatus().getName())
                                .customer(toCustomerSummary(order.getCustomer()))
                                .restaurantBranchId(order.getRestaurantBranch().getId())
                                .deliveryAddressId(order.getAddressId())
                                .items(items)
                                .distinctItemCount(items.size())
                                .totalQuantity(totalQuantity)
                                .subtotal(order.getSubtotal())
                                .deliveryFee(order.getFee())
                                .discountValue(order.getDiscountValue())
                                .totalAmount(order.getTotal())
                                .payment(paymentDto)
                                .note(order.getNote())
                                .build();
        }

        private OrderSummaryCustomerDTO toCustomerSummary(Customer customer) {
                User user = customer.getUser();
                String fullName = (user.getFirstName() == null ? "" : user.getFirstName())
                                + (user.getLastName() == null ? "" : " " + user.getLastName());
                return OrderSummaryCustomerDTO.builder()
                                .customerId(customer.getId())
                                .fullName(fullName.trim())
                                .phone(user.getPhone())
                                .email(user.getEmail())
                                .build();
        }

        private OrderSummaryPaymentDTO toPaymentSummary(PaymentTransaction tx) {
                return OrderSummaryPaymentDTO.builder()
                                .transactionId(tx.getId())
                                .paymentType(tx.getPaymentType())
                                .status(tx.getStatus())
                                .amount(tx.getAmount())
                                .transactionTime(tx.getTransactionTime())
                                .build();
        }

        public List<OrderResponseDTO> getActiveOrdersForRestaurant(UUID restaurantId) {
                List<UUID> branchIds = restaurantBranchRepository.findIdsByRestaurantId(restaurantId);
                if (branchIds.isEmpty()) {
                        return List.of();
                }

                return orderRepository.findActiveByBranchIds(branchIds, FINAL_STATUSES).stream()
                                .map(this::toResponse)
                                .toList();
        }


        private OrderStatus getStatusOrThrow(Integer statusId) {
                return orderStatusRepository.findById(statusId)
                                .orElseThrow(() -> new IllegalArgumentException("Order status not found with id: " + statusId));
        }

        private RestaurantBranch getRestaurantBranchOrThrow(UUID restaurantBranchId) {
                return restaurantBranchRepository.findById(restaurantBranchId)
                                .orElseThrow(() -> new IllegalArgumentException("Restaurant branch not found with id: " + restaurantBranchId));
        }

        @Transactional
        public String changeOrderStatus(UUID orderId, Integer orderStatusId){
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
                OrderStatus status = getStatusOrThrow(orderStatusId);
                order.setStatus(status);
                orderRepository.save(order);

                OrderTracking orderTracking = orderTrackingRepository.findByOrder_Id(orderId)
                        .orElseThrow(() -> new IllegalArgumentException("OrderTracking not found for order id: " + orderId));
                orderTracking.setStatus(status);
                orderTracking.setCreatedAt(LocalDateTime.now());
                orderTrackingRepository.save(orderTracking);

                return "order with orderID: " + orderId + " has changed to status: " + status.getName();
        }

        @Transactional
        public OrderResponseDTO requestReturn(UUID orderId, String reason) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new OrderNotFoundException(orderId));

                if (!STATUS_DELIVERED.equals(order.getStatus().getName())) {
                        throw new InvalidOrderStateException(
                                        "Order can only be returned when its status is " + STATUS_DELIVERED
                                                        + ". Current status: " + order.getStatus().getName());
                }

                OrderStatus returnRequested = getStatusByNameOrThrow(STATUS_RETURN_REQUESTED);
                order.setStatus(returnRequested);
                Order saved = orderRepository.save(order);

                appendTracking(saved, returnRequested, "Return requested by customer: " + reason);

                return toResponse(saved);
        }

        @Transactional
        public OrderResponseDTO approveReturn(UUID orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new OrderNotFoundException(orderId));

                if (!STATUS_RETURN_REQUESTED.equals(order.getStatus().getName())) {
                        throw new InvalidOrderStateException(
                                        "Return can only be approved when order status is " + STATUS_RETURN_REQUESTED
                                                        + ". Current status: " + order.getStatus().getName());
                }

                OrderStatus returned = getStatusByNameOrThrow(STATUS_RETURNED);
                order.setStatus(returned);
                Order saved = orderRepository.save(order);

                appendTracking(saved, returned, "Return approved by restaurant");

                return toResponse(saved);
        }

        @Transactional
        public OrderResponseDTO cancelByCustomer(UUID orderId, String reason) {
                return cancelOrder(orderId, "Cancelled by customer: " + reason);
        }

        @Transactional
        public OrderResponseDTO rejectByRestaurant(UUID orderId, String reason) {
                return cancelOrder(orderId, "Rejected by restaurant: " + reason);
        }

        private OrderResponseDTO cancelOrder(UUID orderId, String trackingDescription) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new OrderNotFoundException(orderId));

                if (!STATUS_PLACED.equals(order.getStatus().getName())) {
                        throw new InvalidOrderStateException(
                                        "Order can only be cancelled when its status is " + STATUS_PLACED
                                                        + ". Current status: " + order.getStatus().getName());
                }

                OrderStatus cancelled = getStatusByNameOrThrow(STATUS_CANCELLED);
                order.setStatus(cancelled);
                Order saved = orderRepository.save(order);

                appendTracking(saved, cancelled, trackingDescription);

                return toResponse(saved);
        }

        private OrderStatus getStatusByNameOrThrow(String name) {
                return orderStatusRepository.findByName(name)
                                .orElseThrow(() -> new IllegalStateException(
                                                "Required order status not configured: " + name));
        }

        private void appendTracking(Order order, OrderStatus status, String description) {
                OrderTracking tracking = new OrderTracking();
                tracking.setOrder(order);
                tracking.setStatus(status);
                tracking.setDescription(description);
                tracking.setCreatedAt(LocalDateTime.now());
                orderTrackingRepository.save(tracking);
        }
}

