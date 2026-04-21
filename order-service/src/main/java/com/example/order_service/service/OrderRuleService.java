package com.example.order_service.service;

import com.example.order_service.model.Order;
import com.example.order_service.model.OrderValidationResult;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderRuleService {

    @Autowired
    private KieContainer kieContainer;

    public OrderValidationResult validateOrder(Order order) {
        // Create a new Drools session for each request
        KieSession kieSession = kieContainer.newKieSession();

        OrderValidationResult result = new OrderValidationResult();

        try {
            // Insert both the order and result into the session
            // Drools rules will read order and modify result
            kieSession.insert(order);
            kieSession.insert(result);

            // Fire all matching rules
            kieSession.fireAllRules();

        } finally {
            kieSession.dispose(); // always dispose session after use
        }

        return result;
    }
}