package com.example.Messenger.Service.Implement;

import com.example.Messenger.Entity.Order;
import com.example.Messenger.Entity.OrderItem;
import com.example.Messenger.Entity.Product;
import com.example.Messenger.Record.OrderItemRequest;
import com.example.Messenger.Record.OrderRequest;
import com.example.Messenger.Repository.OrderItemRepository;
import com.example.Messenger.Repository.OrderRepository;
import com.example.Messenger.Repository.ProductRepository;
import com.example.Messenger.Service.OrderService;
import com.example.Messenger.Service.PendingOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.datatransfer.SystemFlavorMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private final PendingOrderService pendingOrderService;
    private final GmailServiceImp gmailServiceImp;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    public OrderServiceImpl(PendingOrderService pendingOrderService, GmailServiceImp gmailServiceImp, OrderRepository orderRepository, ProductRepository productRepository, OrderItemRepository orderItemRepository) {
        this.pendingOrderService = pendingOrderService;
        this.gmailServiceImp = gmailServiceImp;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = new Order();
        order.setId(generateId(request.customerEmail() + request.customerName()));
        order.setCustomerName(request.customerName());
        order.setAddress(request.address());
        order.setCustomerEmail(request.customerEmail());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PENDING");
        Set<OrderItem> items = new HashSet<>();
        double totalAmount = 0.0;
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.productId()));
            // ✅ Kiểm tra tồn kho
            if (product.getQuantity() < itemReq.quantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            // ✅ Trừ tồn kho đúng một lần
            product.setQuantity(product.getQuantity() - itemReq.quantity());
            productRepository.save(product);
            // ✅ Tạo OrderItem
            OrderItem item = new OrderItem();
            item.setId(generateIdItems(product.getName(), order.getId()));
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setPrice(product.getCurrentPrice());
            // ⚡ Quan trọng: Gắn ngược lại
            item.setOrder(order);
            System.out.println("items " + item.getId());
            totalAmount += product.getCurrentPrice() * itemReq.quantity();
            items.add(item);
        }
        System.out.println("test " + items.stream().toString());
        order.setItems(items);
        order.setTotalAmount(totalAmount);
        gmailServiceImp.sendEmail("nguyentienanh2001.dev@gmail.com", "test", order);
        // ✅ Chỉ cần save order → JPA tự save OrderItem (vì CascadeType.ALL)
        return orderRepository.save(order);
    }

    @Override
    public Order getOrder(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order updateStatus(String id, String status) {
        Order order = getOrder(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public void cancelOrder(String id) {
        Order order = getOrder(id);
        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }
    private String generateId(String name) {
        // Làm sạch tên: bỏ khoảng trắng, viết thường
        String slug = (name == null ? "item" : name.replaceAll("\\s+", "_").toLowerCase());

        // Thêm ngày tháng
        String datePart = LocalDate.now().toString();

        // Sinh phần hash ngắn từ timestamp + tên (đảm bảo không trùng)
        String randomPart = Integer.toHexString((name + System.nanoTime()).hashCode());

        // Gộp lại thành ID hoàn chỉnh
        return randomPart + "_" + datePart + "_" + slug;
    }
    private String generateIdItems(String name,String orderId) {
        // Làm sạch tên: bỏ khoảng trắng, viết thường
        String slug = (name == null ? "item" : name.replaceAll("\\s+", "_").toLowerCase());

        // Thêm ngày tháng
        String datePart = LocalDate.now().toString();

        // Sinh phần hash ngắn từ timestamp + tên (đảm bảo không trùng)
        String randomPart = Integer.toHexString((name + System.nanoTime()).hashCode());

        // Gộp lại thành ID hoàn chỉnh
        return randomPart+"_"+orderId + "_" + datePart + "_" + slug;
    }
    @Override
    public List<Order> getOrdersByUser(String email) {
        return orderRepository.findByCustomerEmail(email);
    }
    @Override
    public String requestOrderConfirmation(OrderRequest request) {
        String token = UUID.randomUUID().toString();
        pendingOrderService.savePendingOrder(token, request);
//        request.customerEmail();
        System.out.println("test requestOrderConfirm " + request.customerEmail());
        gmailServiceImp.sendConfirmationEmail(request.customerEmail(), token);
        return token;
    }

    @Override
    @Transactional
    public Order confirmOrder(String token) {
        OrderRequest request = pendingOrderService.getPendingOrder(token);
        if (request == null) throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn!");

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setCustomerName(request.customerName());
        order.setAddress(request.address());
        order.setCustomerEmail(request.customerEmail());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("CONFIRMED");
        double totalAmount = 0.0;

        Set<OrderItem> items = new HashSet<>();
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.productId()));

            if (product.getQuantity() < itemReq.quantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - itemReq.quantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setId(UUID.randomUUID().toString());
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setPrice(product.getCurrentPrice());
            item.setOrder(order); // ✅ Gắn chiều ngược

            items.add(item); // ✅ Gắn vào tập items

            totalAmount += product.getCurrentPrice() * itemReq.quantity();
        }
        order.setItems(items); // ✅ Gắn vào order
        order.setTotalAmount(totalAmount);
        Order saved = orderRepository.save(order);
        pendingOrderService.deletePendingOrder(token);
        gmailServiceImp.sendSuccessEmail(request.customerEmail(), saved);
        return saved;
    }
}