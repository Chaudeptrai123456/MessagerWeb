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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private final GmailServiceImp gmailServiceImp;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;


    public OrderServiceImpl(GmailServiceImp gmailServiceImp, OrderRepository orderRepository, ProductRepository productRepository, OrderItemRepository orderItemRepository) {
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

        // ✅ Bước 1: Kiểm tra tồn kho
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.productId()));

            if (product.getQuantity() < itemReq.quantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            product.setQuantity(product.getQuantity()-itemReq.quantity());
            productRepository.save(product);
        }

        // ✅ Bước 2: Tạo order + trừ tồn kho + tính tổng tiền
        double totalAmount = 0.0;

        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.productId()));

            // Trừ tồn kho
            product.setQuantity(product.getQuantity() - itemReq.quantity());
            productRepository.save(product);

            // Tạo order item
            OrderItem item = new OrderItem();
            item.setId(generateIdItems(product.getName(), order.getId()));
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setPrice(product.getPrice());
            item.setOrder(order);

            // Tính tổng tiền
            totalAmount += product.getCurrentPrice() * itemReq.quantity();

            items.add(item);
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount); // ✅ Thêm dòng này
        gmailServiceImp.sendEmail("nguyentienanh2001.dev@gmail.com","test",order);
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
    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }
}