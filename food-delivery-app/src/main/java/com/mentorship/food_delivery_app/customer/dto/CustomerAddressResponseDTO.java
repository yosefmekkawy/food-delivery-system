package com.mentorship.food_delivery_app.customer.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddressResponseDTO {

    private UUID id;
    private String label;
    private String city;
    private String street;
    private String building;
    private String apartment;
    private String phoneNumber;
    private String note;
}
