package com.example.order_service.model;

public class OrderValidationResult {

    private boolean valid = true;  // default is valid
    private String message = "Order is valid";

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}