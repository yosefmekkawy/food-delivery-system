package com.mentorship.food_delivery_app.common.notification;

import com.mentorship.food_delivery_app.order.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class LoggingNotificationService implements NotificationService {

    @Override
    public void notifyCustomerOrderPlaced(Order order) {
        String customerEmail = order.getCustomer().getUser().getEmail();
        log.info("[NOTIFICATION → CUSTOMER] Order {} placed successfully. " +
                        "Customer: {} | Total: {} | Status: {}",
                order.getId(),
                customerEmail,
                order.getTotal(),
                order.getStatus().getName());
    }

    @Override
    public void notifyRestaurantNewOrder(Order order, UUID restaurantBranchId) {
        log.info("[NOTIFICATION → RESTAURANT] New order {} received for branch {}. " +
                        "Items: {} | Total: {}",
                order.getId(),
                restaurantBranchId,
                order.getItems().size(),
                order.getTotal());
    }
}


