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
@Table(name = "transaction_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatus {

    @Id
    @Column(name = "status", nullable = false, length = 20)
    private String status;
}

