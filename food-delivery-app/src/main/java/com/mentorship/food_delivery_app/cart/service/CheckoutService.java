package com.mentorship.food_delivery_app.cart.service;

import com.mentorship.food_delivery_app.cart.dto.CheckoutCartRequestDTO;
import com.mentorship.food_delivery_app.cart.dto.CheckoutCartResponseDTO;
import com.mentorship.food_delivery_app.cart.entity.Cart;
import com.mentorship.food_delivery_app.common.notification.NotificationService;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.order.service.OrderService;
import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;
import com.mentorship.food_delivery_app.payment.exceptions.RestaurantBranchRequiredException;
import com.mentorship.food_delivery_app.payment.service.PaymentService;
import com.mentorship.food_delivery_app.restaurant.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CouponService couponService;
    private final NotificationService notificationService;

    @Transactional
    public CheckoutCartResponseDTO checkout(Cart cart, CheckoutCartRequestDTO request) {
        if (cart.getRestaurantId() == null) {
            throw new RestaurantBranchRequiredException(cart.getCustomer().getId());
        }

        UUID couponId = null;
        BigDecimal discountValue = BigDecimal.ZERO;

        if (request.getCouponId() != null) {
            BigDecimal preDiscountSubtotal = cart.getItems().stream()
                    .map(item -> item.getMenuItem().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CouponService.CouponApplicationResult couponResult =
                    couponService.apply(request.getCouponId(), cart.getRestaurantId(), preDiscountSubtotal);

            couponId = couponResult.couponId();
            discountValue = couponResult.discountValue();
        }

        Order order = orderService.createOrderFromCart(cart, request.getNote(), couponId, discountValue);

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
        cart.setRestaurantId(null);

        notificationService.notifyCustomerOrderPlaced(order);
        notificationService.notifyRestaurantNewOrder(order, order.getRestaurantBranchId());

        return new CheckoutCartResponseDTO(
                orderService.toResponse(order),
                paymentService.toResponse(transaction)
        );
    }
}

