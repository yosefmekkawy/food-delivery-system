package com.mentorship.food_delivery_app.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddressRequestDTO {

    @NotBlank(message = "Address label is required")
    @Size(max = 50, message = "Address label must not exceed 50 characters")
    private String label;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must not exceed 255 characters")
    private String street;

    @NotBlank(message = "Building is required")
    @Size(max = 50, message = "Building must not exceed 50 characters")
    private String building;

    @NotBlank(message = "Apartment is required")
    @Size(max = 50, message = "Apartment must not exceed 50 characters")
    private String apartment;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
