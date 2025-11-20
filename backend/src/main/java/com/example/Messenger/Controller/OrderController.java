package com.example.Messenger.Controller;


import com.example.Messenger.Entity.Order;
import com.example.Messenger.Record.OrderRequest;
import com.example.Messenger.Service.Implement.OrderServiceImpl;
import com.example.Messenger.Utils.JwtTokenUtil;
import com.example.Messenger.Utils.KeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderServiceImpl orderService;
    private final JwtTokenUtil jwtTokenUtil;
    public OrderController(OrderServiceImpl orderService, JwtTokenUtil jwtTokenUtil, JwtTokenUtil jwtTokenUtil1) {
        this.orderService = orderService;
        this.jwtTokenUtil = jwtTokenUtil1;
    }

    @KafkaListener(topics = "analysis-topic", groupId = "order-service-group")
  public void requestOrder(OrderRequest request
    ) {
        Map<String, Object> user = jwtTokenUtil.getUserFromToken(
                request.token(),
                KeyUtil.loadOrCreateKeyPair().getPublic()
        );
        var orderRequest =  new OrderRequest(user.get("username").toString(),user.get("email").toString(),request.address(),request.items(),request.token());
        orderService.requestOrderConfirmation(orderRequest);
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
    @GetMapping("/user")
    public ResponseEntity<List<Order>> getOrdersByUser(HttpServletRequest request) {
        System.out.println("test get order by 8081");

        // Lấy bearer token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);

        // Giải token -> lấy email (username)
        Map<String, Object> user = jwtTokenUtil.getUserFromToken(
                token,
                KeyUtil.loadOrCreateKeyPair().getPublic()
        );
        System.out.println("test " + user.get("email"));
        List<Order> orders = orderService.getOrdersByUser(user.get("email").toString());
        return ResponseEntity.ok(orders);
    }

}