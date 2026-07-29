package com.mentorship.food_delivery_app.restaurant.exceptions;

import java.math.BigDecimal;

public class InvalidCouponException extends RuntimeException {
    public InvalidCouponException(String message) {
        super(message);
    }

    public static InvalidCouponException inactive(java.util.UUID couponId) {
        return new InvalidCouponException("Coupon " + couponId + " is not active or has expired.");
    }

    public static InvalidCouponException belowMinOrder(BigDecimal minOrderValue, BigDecimal orderSubtotal) {
        return new InvalidCouponException(
                "Order subtotal " + orderSubtotal + " is below the minimum required " + minOrderValue + " for this coupon.");
    }

    public static InvalidCouponException wrongRestaurant(java.util.UUID couponRestaurantId, java.util.UUID branchId) {
        return new InvalidCouponException(
                "Coupon belongs to restaurant " + couponRestaurantId + " but order is for branch " + branchId + ".");
    }
}

