package com.mentorship.food_delivery_app.payment.repository;

import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findTopByOrder_IdOrderByTransactionTimeDesc(UUID orderId);
}
