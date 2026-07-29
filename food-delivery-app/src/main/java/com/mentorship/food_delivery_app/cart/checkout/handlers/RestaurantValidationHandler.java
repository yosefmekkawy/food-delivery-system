package com.mentorship.food_delivery_app.cart.checkout.handlers;

import com.mentorship.food_delivery_app.cart.checkout.AbstractCheckoutHandler;
import com.mentorship.food_delivery_app.cart.checkout.CheckoutContext;
import com.mentorship.food_delivery_app.payment.exceptions.RestaurantBranchRequiredException;

public class RestaurantValidationHandler extends AbstractCheckoutHandler {

    @Override
    public void handle(CheckoutContext context) {
        if (context.getCart().getRestaurantId() == null) {
            throw new RestaurantBranchRequiredException(context.getCart().getCustomer().getId());
        }
        proceed(context);
    }
}
