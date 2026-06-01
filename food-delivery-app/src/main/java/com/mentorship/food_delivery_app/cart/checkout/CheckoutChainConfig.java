package com.mentorship.food_delivery_app.cart.checkout;

import com.mentorship.food_delivery_app.cart.checkout.handlers.*;
import com.mentorship.food_delivery_app.common.notification.NotificationService;
import com.mentorship.food_delivery_app.order.service.OrderService;
import com.mentorship.food_delivery_app.payment.service.PaymentService;
import com.mentorship.food_delivery_app.restaurant.service.CouponService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CheckoutChainConfig {

    @Bean
    public CheckoutHandler checkoutChain(
            CouponService couponService,
            OrderService orderService,
            PaymentService paymentService,
            NotificationService notificationService
    ) {
        RestaurantValidationHandler restaurantValidation = new RestaurantValidationHandler();
        CouponApplicationHandler couponApplication = new CouponApplicationHandler(couponService);
        OrderCreationHandler orderCreation = new OrderCreationHandler(orderService);
        PaymentProcessingHandler paymentProcessing = new PaymentProcessingHandler(paymentService);
        CartClearingHandler cartClearing = new CartClearingHandler();
        NotificationHandler notification = new NotificationHandler(notificationService);

        restaurantValidation.setNext(couponApplication);
        couponApplication.setNext(orderCreation);
        orderCreation.setNext(paymentProcessing);
        paymentProcessing.setNext(cartClearing);
        cartClearing.setNext(notification);

        return restaurantValidation;
    }
}
