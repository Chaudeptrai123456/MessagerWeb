package com.example.Messenger.Service;

import com.example.Messenger.Entity.Order;
import com.example.Messenger.Record.OrderRequest;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderRequest request);
    Order getOrder(String id);
    List<Order> getAllOrders();
    Order updateStatus(String id, String status);
    void cancelOrder(String id);
    public List<Order> getOrdersByUser(String userId);
    public String requestOrderConfirmation(OrderRequest request);
    public Order confirmOrder(String token) ;
}