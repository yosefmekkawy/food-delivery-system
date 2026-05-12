package com.mentorship.food_delivery_app.order.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mentorship.food_delivery_app.order.dto.OrderResponseDTO;
import com.mentorship.food_delivery_app.order.service.OrderService;


@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/{orderId}")
    public OrderResponseDTO getOrderById(@PathVariable UUID orderId) {
        return orderService.getOrderById(orderId); 
    }

    @GetMapping("/restaurant/{restaurantId}/active")
    public List<OrderResponseDTO> getActiveOrdersForRestaurant(@PathVariable UUID restaurantId) {
        return orderService.getActiveOrdersForRestaurant(restaurantId);
    }

    @PatchMapping("/{orderId}/status")
    public String changeOrderStatus(@PathVariable UUID orderId, @RequestParam Integer statusId) {
        return orderService.changeOrderStatus(orderId, statusId);
    }
    
    
}
