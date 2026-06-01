package com.mentorship.food_delivery_app.cart.checkout.handlers;

import com.mentorship.food_delivery_app.cart.checkout.AbstractCheckoutHandler;
import com.mentorship.food_delivery_app.cart.checkout.CheckoutContext;
import com.mentorship.food_delivery_app.restaurant.service.CouponService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class CouponApplicationHandler extends AbstractCheckoutHandler {

    private final CouponService couponService;

    @Override
    public void handle(CheckoutContext context) {
        if (context.getRequest().getCouponId() != null) {
            BigDecimal preDiscountSubtotal = context.getCart().getItems().stream()
                    .map(item -> item.getMenuItem().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CouponService.CouponApplicationResult result = couponService.apply(
                    context.getRequest().getCouponId(),
                    context.getCart().getRestaurantId(),
                    preDiscountSubtotal
            );

            context.setCouponId(result.couponId());
            context.setDiscountValue(result.discountValue());
        }

        proceed(context);
    }
}
