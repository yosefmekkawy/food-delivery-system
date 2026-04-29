package com.mentorship.food_delivery_app.cart.service;

import com.mentorship.food_delivery_app.cart.dto.CheckoutCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartResponseDTO;
import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.order.service.OrderService;
import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;
import com.mentorship.food_delivery_app.payment.exceptions.RestaurantBranchRequiredException;
import com.mentorship.food_delivery_app.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @Transactional
    public CheckoutCartResponseDTO checkout(Cart cart, CheckoutCartRequestDTO request) {
        if (cart.getRestaurantId() == null) {
            throw new RestaurantBranchRequiredException(cart.getCustomer().getId());
        }

        Order order = orderService.createOrderFromCart(cart, request.getNote());
        PaymentTransaction transaction = paymentService.processPayment(
                new PaymentService.PaymentCommand(
                        order,
                        cart.getCustomer(),
                        cart.getRestaurantId(),
                        request.getPaymentIntegrationType(),
                        order.getTotal()
                )
        );

        cart.clearItems();
        cart.setNotes(null);
        cart.setRestaurantId(null);

        return new CheckoutCartResponseDTO(
                orderService.toResponse(order),
                paymentService.toResponse(transaction)
        );
    }
}

