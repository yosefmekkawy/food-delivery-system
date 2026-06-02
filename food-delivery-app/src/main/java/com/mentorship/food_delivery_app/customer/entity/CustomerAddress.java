package com.mentorship.food_delivery_app.customer.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_address_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_address_customer_id", nullable = false)
    private Customer customer;

    @Column(name = "customer_address_label", nullable = false, length = 50)
    private String label;

    @Column(name = "customer_address_city", nullable = false, length = 100)
    private String city;

    @Column(name = "customer_address_street", nullable = false, length = 255)
    private String street;

    @Column(name = "customer_address_building", nullable = false, length = 50)
    private String building;

    @Column(name = "customer_address_apartment", nullable = false, length = 50)
    private String apartment;

    @Column(name = "customer_address_phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "customer_address_note", length = 500)
    private String note;

}
