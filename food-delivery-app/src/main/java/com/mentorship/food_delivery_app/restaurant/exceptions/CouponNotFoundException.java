package com.mentorship.food_delivery_app.restaurant.exceptions;

import java.util.UUID;

public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(UUID couponId) {
        super("Coupon not found with id: " + couponId);
    }
}

