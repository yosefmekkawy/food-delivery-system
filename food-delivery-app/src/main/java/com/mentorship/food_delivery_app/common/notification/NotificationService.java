package com.mentorship.food_delivery_app.common.notification;

import com.mentorship.food_delivery_app.order.entity.Order;

import java.util.UUID;

public interface NotificationService {

    void notifyCustomerOrderPlaced(Order order);

    void notifyRestaurantNewOrder(Order order, UUID restaurantBranchId);
}


