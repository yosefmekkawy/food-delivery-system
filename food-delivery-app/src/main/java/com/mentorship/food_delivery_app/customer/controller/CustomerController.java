package com.mentorship.food_delivery_app.customer.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mentorship.food_delivery_app.customer.dto.CustomerAddressRequestDTO;
import com.mentorship.food_delivery_app.customer.dto.CustomerAddressResponseDTO;
import com.mentorship.food_delivery_app.customer.dto.CustomerOrderResponse;
import com.mentorship.food_delivery_app.customer.dto.CustomerPreferredPaymentResponseDTO;
import com.mentorship.food_delivery_app.customer.dto.CustomerStatusResponseDTO;
import com.mentorship.food_delivery_app.customer.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{customerId}/orders")
    public ResponseEntity<List<CustomerOrderResponse>> getCustomerOrders(@PathVariable UUID customerId) {
        return ResponseEntity.ok(customerService.getCustomerOrders(customerId));
    }

    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<List<CustomerAddressResponseDTO>> getCustomerAddresses(@PathVariable UUID customerId) {
        return ResponseEntity.ok(customerService.getCustomerAddress(customerId));
    }

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<CustomerAddressResponseDTO> createCustomerAddress(
            @PathVariable UUID customerId,
            @Valid @RequestBody CustomerAddressRequestDTO addressDTO) {
        CustomerAddressResponseDTO response = customerService.setCustomerAddress(addressDTO, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<CustomerAddressResponseDTO> updateCustomerAddress(
            @PathVariable UUID addressId,
            @Valid @RequestBody CustomerAddressRequestDTO addressDTO) {
        return ResponseEntity.ok(customerService.updateCustomerAddress(addressDTO, addressId));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> removeCustomerAddress(@PathVariable UUID addressId) {
        return ResponseEntity.ok(customerService.removeCustomerAddress(addressId));
    }

    @GetMapping("/{customerId}/preferred-payment")
    public ResponseEntity<CustomerPreferredPaymentResponseDTO> getPreferredPayment(@PathVariable UUID customerId) {
        return ResponseEntity.ok(customerService.getCustomerPreferredPayment(customerId));
    }

    @PutMapping("/{customerId}/preferred-payment")
    public ResponseEntity<CustomerPreferredPaymentResponseDTO> setPreferredPayment(
            @PathVariable UUID customerId,
            @RequestParam Long preferredPaymentId) {
        return ResponseEntity.ok(customerService.setCustomerPreferredPayment(preferredPaymentId, customerId));
    }

    @PatchMapping("/{customerId}/deactive")
    public ResponseEntity<CustomerStatusResponseDTO> deactivateCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(customerService.deactivateCustomer(customerId));
    }
}