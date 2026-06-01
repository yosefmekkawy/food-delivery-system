package com.mentorship.food_delivery_app.cart.checkout.handlers;

import com.mentorship.food_delivery_app.cart.checkout.AbstractCheckoutHandler;
import com.mentorship.food_delivery_app.cart.checkout.CheckoutContext;
import com.mentorship.food_delivery_app.common.notification.NotificationService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationHandler extends AbstractCheckoutHandler {

    private final NotificationService notificationService;

    @Override
    public void handle(CheckoutContext context) {
        notificationService.notifyCustomerOrderPlaced(context.getOrder());
        notificationService.notifyRestaurantNewOrder(context.getOrder(), context.getOrder().getRestaurantBranchId());
        proceed(context);
    }
}
