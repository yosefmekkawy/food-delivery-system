package com.mentorship.food_delivery_app.payment.service;

import com.mentorship.food_delivery_app.payment.dto.PaymentTransactionResponseDTO;
import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;
import com.mentorship.food_delivery_app.payment.entity.PaymentTypeConfig;
import com.mentorship.food_delivery_app.payment.exceptions.PaymentConfigurationNotFoundException;
import com.mentorship.food_delivery_app.payment.gateway.PaymentGateway;
import com.mentorship.food_delivery_app.payment.repository.PaymentTransactionRepository;
import com.mentorship.food_delivery_app.payment.repository.PaymentTypeConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DefaultPaymentService implements PaymentService {

    private final PaymentTypeConfigRepository paymentTypeConfigRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentGateway paymentGateway;

    @Override
    @Transactional
    public PaymentTransaction processPayment(PaymentCommand command) {
        String normalizedPaymentType = command.paymentIntegrationType().trim().toUpperCase(Locale.ROOT);
        PaymentTypeConfig config = paymentTypeConfigRepository.findByPaymentIntegrationTypeIgnoreCase(normalizedPaymentType)
                .orElseThrow(() -> new PaymentConfigurationNotFoundException(normalizedPaymentType));

        PaymentGateway.GatewayResult gatewayResult = paymentGateway.process(
                new PaymentGateway.PaymentRequest(
                        command.order().getId(),
                        command.customer().getId(),
                        command.restaurantBranchId(),
                        normalizedPaymentType,
                        command.amount()
                ),
                config.getConfigDetails()
        );

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setStatus(gatewayResult.status());
        transaction.setOrder(command.order());
        transaction.setPaymentType(normalizedPaymentType);
        transaction.setCustomer(command.customer());
        transaction.setRestaurantBranchId(command.restaurantBranchId());
        transaction.setAmount(command.amount());
        transaction.setTransactionTime(gatewayResult.processedAt());

        return paymentTransactionRepository.save(transaction);
    }

    @Override
    public PaymentTransactionResponseDTO toResponse(PaymentTransaction transaction) {
        return new PaymentTransactionResponseDTO(
                transaction.getId(),
                transaction.getStatus(),
                transaction.getPaymentType(),
                transaction.getAmount(),
                transaction.getTransactionTime()
        );
    }
}

