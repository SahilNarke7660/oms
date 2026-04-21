package com.example.order_service.controller;

import com.example.order_service.model.Order;
import com.example.order_service.model.OrderResponse;
import com.example.order_service.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // Create order
    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return service.createOrder(order);
    }

    // Get all orders (simple list, no user enrichment)
    @GetMapping
    public List<Order> getAllOrders() {
        return service.getAllOrders();
    }

    // Get order by id (WITH USER DETAILS - integrated response)
    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return service.getOrderById(id);
    }

    // Delete order
    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id) {
        service.deleteOrder(id);
        return "Order deleted successfully";
    }
}