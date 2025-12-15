package com.example.foodapp.Domain;

import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {
    private String orderId;
    private String userId;
    private ArrayList<Foods> items;
    private double totalPrice;
    private long timestamp;
    private String status; // Misal: "Menunggu Konfirmasi", "Sedang Dibuat", "Selesai"

    public Order() {
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<Foods> getItems() {
        return items;
    }

    public void setItems(ArrayList<Foods> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}