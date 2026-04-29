package com.mentorship.food_delivery_app.order.service;

import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.cart.entity.CartItem;
import com.mentorship.food_delivery_app.order.dto.OrderItemResponseDTO;
import com.mentorship.food_delivery_app.order.dto.OrderResponseDTO;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.order.entity.OrderItem;
import com.mentorship.food_delivery_app.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal DELIVERY_FEE = BigDecimal.ZERO;
    private static final String DEFAULT_ORDER_STATUS = "PLACED";

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrderFromCart(Cart cart, String checkoutNote) {
        List<OrderItem> orderItems = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getMenuItem().getId()))
                .map(this::mapToOrderItem)
                .toList();

        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setSubtotal(subtotal);
        order.setFee(DELIVERY_FEE);
        order.setTotal(subtotal.add(DELIVERY_FEE));
        order.setOrderedAt(LocalDateTime.now());
        order.setStatus(DEFAULT_ORDER_STATUS);
        order.setNote(StringUtils.hasText(checkoutNote) ? checkoutNote : cart.getNotes());
        orderItems.forEach(order::addItem);

        return orderRepository.save(order);
    }

    private OrderItem mapToOrderItem(CartItem cartItem) {
        BigDecimal unitPrice = cartItem.getMenuItem().getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new OrderItem(
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
                order.getStatus(),
                order.getOrderedAt()
        );
    }
}

