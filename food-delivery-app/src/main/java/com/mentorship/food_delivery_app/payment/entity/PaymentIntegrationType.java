package com.mentorship.food_delivery_app.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_integration_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntegrationType {

    @Id
    @Column(name = "payment_integration_type_name", nullable = false, length = 20)
    private String name;
}

