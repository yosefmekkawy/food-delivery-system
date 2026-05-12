package com.mentorship.food_delivery_app.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mentorship.food_delivery_app.customer.entity.Customer;
import com.mentorship.food_delivery_app.order.entity.Order;
import com.mentorship.food_delivery_app.payment.dto.PaymentTransactionResponseDTO;
import com.mentorship.food_delivery_app.payment.entity.PaymentTransaction;
import com.mentorship.food_delivery_app.payment.entity.PaymentTypeConfig;
import com.mentorship.food_delivery_app.payment.exceptions.PaymentConfigurationNotFoundException;
import com.mentorship.food_delivery_app.payment.gateway.PaymentGateway;
import com.mentorship.food_delivery_app.payment.repository.PaymentTransactionRepository;
import com.mentorship.food_delivery_app.payment.repository.PaymentTypeConfigRepository;

@ExtendWith(MockitoExtension.class)
class DefaultPaymentServiceTest {

    @Mock
    private PaymentTypeConfigRepository paymentTypeConfigRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private DefaultPaymentService paymentService;

    @Test
    void processPayment_shouldNormalizeTypeAndPersistTransaction() {
        Order order = new Order();
        order.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        Customer customer = new Customer();
        customer.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));

        PaymentTypeConfig config = new PaymentTypeConfig(1L, "CARD", "{\"provider\":\"simulated\"}");
        LocalDateTime processedAt = LocalDateTime.now();

        when(paymentTypeConfigRepository.findByPaymentIntegrationTypeIgnoreCase("CARD"))
                .thenReturn(Optional.of(config));
        when(paymentGateway.process(any(PaymentGateway.PaymentRequest.class), any(String.class)))
                .thenReturn(new PaymentGateway.GatewayResult("COMPLETED", processedAt));
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> {
            PaymentTransaction transaction = invocation.getArgument(0);
            transaction.setId(UUID.randomUUID());
            return transaction;
        });

        UUID branchId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        PaymentTransaction transaction = paymentService.processPayment(
                new PaymentService.PaymentCommand(order, customer, branchId, " card ", new BigDecimal("19.99"))
        );

        ArgumentCaptor<PaymentGateway.PaymentRequest> requestCaptor = ArgumentCaptor.forClass(PaymentGateway.PaymentRequest.class);
        verify(paymentGateway).process(requestCaptor.capture(), any(String.class));

        PaymentGateway.PaymentRequest request = requestCaptor.getValue();
        assertThat(request.orderId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(request.customerId()).isEqualTo(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        assertThat(request.restaurantBranchId()).isEqualTo(branchId);
        assertThat(request.paymentIntegrationType()).isEqualTo("CARD");
        assertThat(request.amount()).isEqualByComparingTo("19.99");

        assertThat(transaction.getStatus()).isEqualTo("COMPLETED");
        assertThat(transaction.getPaymentType()).isEqualTo("CARD");
        assertThat(transaction.getCustomer()).isSameAs(customer);
        assertThat(transaction.getOrder()).isSameAs(order);
        assertThat(transaction.getRestaurantBranchId()).isEqualTo(branchId);
        assertThat(transaction.getAmount()).isEqualByComparingTo("19.99");
        assertThat(transaction.getTransactionTime()).isEqualTo(processedAt);
        assertThat(transaction.getId()).isNotNull();

        PaymentTransactionResponseDTO response = paymentService.toResponse(transaction);
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getPaymentIntegrationType()).isEqualTo("CARD");
        assertThat(response.getAmount()).isEqualByComparingTo("19.99");
    }

    @Test
    void processPayment_shouldFailWhenConfigMissing() {
        Order order = new Order();
        order.setId(UUID.fromString("44444444-4444-4444-4444-444444444444"));

        Customer customer = new Customer();
        customer.setId(UUID.fromString("55555555-5555-5555-5555-555555555555"));

        when(paymentTypeConfigRepository.findByPaymentIntegrationTypeIgnoreCase("WALLET"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processPayment(
                new PaymentService.PaymentCommand(order, customer, UUID.fromString("66666666-6666-6666-6666-666666666666"), "wallet", new BigDecimal("12.00"))
        ))
                .isInstanceOf(PaymentConfigurationNotFoundException.class)
                .hasMessage("Payment integration type 'WALLET' is not configured");
    }
}

