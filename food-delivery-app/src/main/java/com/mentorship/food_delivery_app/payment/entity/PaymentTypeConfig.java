package com.mentorship.food_delivery_app.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_type_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTypeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_type_config_id")
    private Long id;

    @Column(name = "payment_integration_type", nullable = false, length = 20)
    private String paymentIntegrationType;

    @Column(name = "config_details", nullable = false, columnDefinition = "TEXT")
    private String configDetails;
}

