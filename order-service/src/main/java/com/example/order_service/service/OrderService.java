package com.example.order_service.service;

import com.example.order_service.client.UserClient;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderResponse;
import com.example.order_service.model.OrderValidationResult;
import com.example.order_service.repository.OrderRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderRuleService orderRuleService;  // Drools

    public Order createOrder(Order order) {

        // Step 1: Validate user exists via User Service (Feign)
        try {
            userClient.getUserById(order.getUserId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("User not found. Cannot place order.");
        }

        // Step 2: Run Drools rules against the order
        OrderValidationResult result = orderRuleService.validateOrder(order);

        if (!result.isValid()) {
            // Rule was violated — reject the order
            throw new RuntimeException(result.getMessage());
        }

        // Step 3: All validations passed — save order
        return repository.save(order);
    }

    public List<Order> getAllOrders() {
        return repository.findAll();
    }

    public OrderResponse getOrderById(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Object user = userClient.getUserById(order.getUserId());
        return new OrderResponse(order.getId(), order.getProduct(),
                                 order.getQuantity(), user);
    }

    public void deleteOrder(Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Order not found: " + id);
        repository.deleteById(id);
    }
}