package com.mentorship.food_delivery_app.restaurant.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mentorship.food_delivery_app.restaurant.entity.Coupon;
import com.mentorship.food_delivery_app.restaurant.entity.CouponDiscountType;
import com.mentorship.food_delivery_app.restaurant.exceptions.CouponNotFoundException;
import com.mentorship.food_delivery_app.restaurant.exceptions.InvalidCouponException;
import com.mentorship.food_delivery_app.restaurant.repository.CouponRepository;
import com.mentorship.food_delivery_app.restaurant.repository.RestaurantBranchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final RestaurantBranchRepository restaurantBranchRepository;

    public CouponApplicationResult apply(UUID couponId, UUID restaurantBranchId, BigDecimal orderSubtotal) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(couponId));

        validateCouponIsActive(coupon);
        validateRestaurant(coupon, restaurantBranchId);
        validateMinOrderValue(coupon, orderSubtotal);

        BigDecimal discount = calculateDiscount(coupon, orderSubtotal);
        return new CouponApplicationResult(coupon.getId(), discount);
    }

    private void validateCouponIsActive(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        boolean active = Boolean.TRUE.equals(coupon.getIsActive())
                && now.isAfter(coupon.getAvailableFrom())
                && now.isBefore(coupon.getAvailableTo());
        if (!active) {
            throw InvalidCouponException.inactive(coupon.getId());
        }
    }

    private void validateRestaurant(Coupon coupon, UUID restaurantBranchId) {
        UUID restaurantId = restaurantBranchRepository.findById(restaurantBranchId)
                .map(branch -> branch.getRestaurant().getId())
                .orElse(null);
        if (!coupon.getRestaurantId().equals(restaurantId)) {
            throw InvalidCouponException.wrongRestaurant(coupon.getRestaurantId(), restaurantBranchId);
        }
    }

    private void validateMinOrderValue(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getMinOrderValue() != null
                && subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw InvalidCouponException.belowMinOrder(coupon.getMinOrderValue(), subtotal);
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getAmount() == null || coupon.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (coupon.getDiscountType() == CouponDiscountType.PERCENTAGE) {
            discount = subtotal.multiply(coupon.getAmount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getAmount();
        }
        
        return discount;
    }

    public record CouponApplicationResult(UUID couponId, BigDecimal discountValue) {}
}


