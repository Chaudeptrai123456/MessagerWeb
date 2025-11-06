package com.example.Messenger.Controller;


import com.example.Messenger.Entity.Order;
import com.example.Messenger.Record.OrderRequest;
import com.example.Messenger.Service.Implement.OrderServiceImpl;
import com.example.Messenger.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderServiceImpl orderService;

    public OrderController(OrderServiceImpl orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "analysis-topic", groupId = "order-service-group")
  public void requestOrder(OrderRequest request) {
        System.out.println("📥 Received order from: " + request.customerName());
        String token = orderService.requestOrderConfirmation(request);
        System.out.println("📧 Đã gửi email xác nhận đến " + request.customerEmail());
    }
    @GetMapping("/confirm")
    public ResponseEntity<String> confirmOrder(@RequestParam String token) {
        Order order = orderService.confirmOrder(token);
        return ResponseEntity.ok("✅ Đơn hàng " + order.getId() + " đã được xác nhận!");
    }
    // 🔍 Lấy tất cả đơn hàng
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // 🔎 Lấy chi tiết 1 đơn
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    // 🟡 Cập nhật trạng thái
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable String id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    // Huỷ đơn
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable String userId) {
        List<Order> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }
}