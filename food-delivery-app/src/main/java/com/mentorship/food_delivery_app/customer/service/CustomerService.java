package com.mentorship.food_delivery_app.customer.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mentorship.food_delivery_app.customer.dto.CustomerAddressRequestDTO;
import com.mentorship.food_delivery_app.customer.dto.CustomerAddressResponseDTO;
import com.mentorship.food_delivery_app.customer.dto.CustomerOrderItemResponse;
import com.mentorship.food_delivery_app.customer.dto.CustomerOrderResponse;
import com.mentorship.food_delivery_app.customer.dto.CustomerPreferredPaymentResponseDTO;
import com.mentorship.food_delivery_app.customer.dto.CustomerStatusResponseDTO;
import com.mentorship.food_delivery_app.customer.entity.Customer;
import com.mentorship.food_delivery_app.customer.entity.CustomerAddress;
import com.mentorship.food_delivery_app.user.entity.User;
import com.mentorship.food_delivery_app.customer.exceptions.CustomerNotFoundException;
import com.mentorship.food_delivery_app.customer.exceptions.CustomerObjectrdersNotFoundException;
import com.mentorship.food_delivery_app.customer.repository.CustomerAddressRepository;
import com.mentorship.food_delivery_app.customer.repository.CustomerRepository;
import com.mentorship.food_delivery_app.order.repository.OrderRepository;
import com.mentorship.food_delivery_app.payment.entity.PaymentTypeConfig;
import com.mentorship.food_delivery_app.payment.repository.PaymentTypeConfigRepository;

import jakarta.transaction.Transactional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerAddressRepository customerAddressRepository;
    
    @Autowired
    private PaymentTypeConfigRepository paymentTypeConfigRepository;

    @Autowired
    private OrderRepository orderRepository;
    
    public List<CustomerOrderResponse> getCustomerOrders(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        List<Object[]> results = customerRepository.findLatest10OrderRowsByCustomerId(customerId);

        if (results.isEmpty()) {
            throw new CustomerObjectrdersNotFoundException("No orders found for customer id: " + customerId);
        }

        Map<UUID, CustomerOrderResponse> groupedOrders = new LinkedHashMap<>();

        for (Object[] row : results) {
            UUID orderId = (UUID) row[0];

            CustomerOrderResponse response = groupedOrders.computeIfAbsent(orderId, id -> {
                CustomerOrderResponse orderResponse = new CustomerOrderResponse();
                orderResponse.setOrderId(id);
                orderResponse.setStatus(row[1] != null ? row[1].toString() : null);
                orderResponse.setOrderDate(toInstant(row[2]));
                orderResponse.setRestaurantName((String) row[3]);
                orderResponse.setTotal((BigDecimal) row[4]);
                orderResponse.setItems(new ArrayList<>());
                return orderResponse;
            });

            CustomerOrderItemResponse item = new CustomerOrderItemResponse(
                    (String) row[5],
                    row[6] == null ? 0 : ((Number) row[6]).intValue(),
                    (BigDecimal) row[7]);

            response.getItems().add(item);
        }

        return new ArrayList<>(groupedOrders.values());
    }

    private Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Instant instant) {
            return instant;
        }

        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }

        throw new IllegalArgumentException("Unsupported date type: " + value.getClass().getName());
    }

    public List<CustomerAddressResponseDTO> getCustomerAddress(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        List<CustomerAddress> addresses = customerAddressRepository.findByCustomerId(customerId);
        if (addresses.isEmpty()) {
            throw new CustomerObjectrdersNotFoundException("No addresses found for customer id: " + customerId);
        }
        List<CustomerAddressResponseDTO> response = addresses.stream()
                .map(addr -> new CustomerAddressResponseDTO(
                        addr.getId(),
                addr.getLabel(),
                        addr.getCity(),
                        addr.getStreet(),
                        addr.getBuilding(),
                        addr.getApartment(),
                        addr.getPhoneNumber(),
                        addr.getNote()
                ))
                .toList();
        return response;
    }

    @Transactional
    public CustomerAddressResponseDTO setCustomerAddress(CustomerAddressRequestDTO addressDTO, UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setLabel(addressDTO.getLabel());
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setStreet(addressDTO.getStreet());
        address.setBuilding(addressDTO.getBuilding());
        address.setApartment(addressDTO.getApartment());
        address.setPhoneNumber(addressDTO.getPhoneNumber());
        address.setNote(addressDTO.getNote());

        customerAddressRepository.save(address);
        return new CustomerAddressResponseDTO(
                address.getId(),
            address.getLabel(),
                address.getCity(),
                address.getStreet(),
                address.getBuilding(),
                address.getApartment(),
                address.getPhoneNumber(),
                address.getNote()
        );
    }

    @Transactional
    public String removeCustomerAddress(UUID addressId) {
        if (!customerAddressRepository.existsById(addressId)) {
            throw new CustomerObjectrdersNotFoundException("No addresses found for address id: " + addressId);
        }
        if (orderRepository.existsByAddressId(addressId)) {
            throw new CustomerObjectrdersNotFoundException(
                    "Cannot delete address because it is referenced by one or more orders: " + addressId);
        }
        customerAddressRepository.deleteById(addressId);
        return "Address removed successfully";
    }

    @Transactional
    public CustomerAddressResponseDTO updateCustomerAddress(CustomerAddressRequestDTO addressDTO, UUID addressId) {
        CustomerAddress address = customerAddressRepository.findById(addressId).orElseThrow(() -> new CustomerObjectrdersNotFoundException("No addresses found for address id: " + addressId));
        address.setLabel(addressDTO.getLabel());
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setStreet(addressDTO.getStreet());
        address.setBuilding(addressDTO.getBuilding());
        address.setApartment(addressDTO.getApartment());
        address.setPhoneNumber(addressDTO.getPhoneNumber());
        address.setNote(addressDTO.getNote());

        customerAddressRepository.save(address);
        return new CustomerAddressResponseDTO(
                address.getId(),
            address.getLabel(),
                address.getCity(),
                address.getStreet(),
                address.getBuilding(),
                address.getApartment(),
                address.getPhoneNumber(),
                address.getNote()
        );
    }

    public CustomerPreferredPaymentResponseDTO getCustomerPreferredPayment(UUID customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));
        PaymentTypeConfig preferredPayment = customer.getPreferredPayment();
        if (preferredPayment == null || preferredPayment.getId() == null) {
            throw new CustomerObjectrdersNotFoundException(
                    "Preferred payment is not configured for customer id: " + customerId);
        }
        return CustomerPreferredPaymentResponseDTO.builder()
                .preferredPaymentId(customer.getPreferredPayment().getId())
                .paymentIntegrationType(customer.getPreferredPayment().getPaymentIntegrationType())
                .build();
    }
    
    @Transactional
    public CustomerStatusResponseDTO deactivateCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        User user = customer.getUser();
        if (Boolean.TRUE.equals(user.getIsEnabled())) {
            user.setIsEnabled(false);
        }
        return CustomerStatusResponseDTO.builder()
                .customerId(customer.getId())
                .active(false)
                .build();
    }

    @Transactional
    public CustomerPreferredPaymentResponseDTO setCustomerPreferredPayment(Long preferredPaymentId, UUID customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));
        PaymentTypeConfig preferredPayment = paymentTypeConfigRepository.findById(preferredPaymentId)
                .orElseThrow(() -> new CustomerObjectrdersNotFoundException(
                        "Payment type config not found for id: " + preferredPaymentId));
        customer.setPreferredPayment(preferredPayment);
        customerRepository.save(customer);
        return CustomerPreferredPaymentResponseDTO.builder()
                .preferredPaymentId(customer.getPreferredPayment().getId())
                .paymentIntegrationType(customer.getPreferredPayment().getPaymentIntegrationType())
                .build();
    }
}
