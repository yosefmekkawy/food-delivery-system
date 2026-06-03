package com.mentorship.food_delivery_app.cart.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for checking out the customer's active cart.
 */
public class CheckoutCartRequestDTO {

    private String note;

    @NotBlank(message = "Payment integration type is required")
    private String paymentIntegrationType;

    private UUID couponId;

    public CheckoutCartRequestDTO() {
    }

    public CheckoutCartRequestDTO(String note, String paymentIntegrationType) {
        this.note = note;
        this.paymentIntegrationType = paymentIntegrationType;
    }

    public CheckoutCartRequestDTO(String note, String paymentIntegrationType, UUID couponId) {
        this.note = note;
        this.paymentIntegrationType = paymentIntegrationType;
        this.couponId = couponId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPaymentIntegrationType() {
        return paymentIntegrationType;
    }

    public void setPaymentIntegrationType(String paymentIntegrationType) {
        this.paymentIntegrationType = paymentIntegrationType;
    }

    public UUID getCouponId() {
        return couponId;
    }

    public void setCouponId(UUID couponId) {
        this.couponId = couponId;
    }
}

