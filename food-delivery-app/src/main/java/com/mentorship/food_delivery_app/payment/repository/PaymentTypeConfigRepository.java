package com.mentorship.food_delivery_app.payment.repository;

import com.mentorship.food_delivery_app.payment.entity.PaymentTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTypeConfigRepository extends JpaRepository<PaymentTypeConfig, Long> {

    Optional<PaymentTypeConfig> findByPaymentIntegrationTypeIgnoreCase(String paymentIntegrationType);
}

