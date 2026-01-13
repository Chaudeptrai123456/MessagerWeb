package com.example.Messenger.Controller.Owner;

import com.example.Messenger.Record.DashboardMetricsDTO;
import com.example.Messenger.Record.UserResponse;
import com.example.Messenger.Record.WarehouseRequest;
import com.example.Messenger.Record.assignManagerToWarehouseRequest;
import com.example.Messenger.Repository.UserRepository;
import com.example.Messenger.Service.Implement.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Messenger.Entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/owner")
public class WarehouseController {
    private final UserService userService;
    private final WarehouseEconomicService warehouseEconomicService;
    private final WarehouseAssignmentService warehouseAssignmentService;
    private final ProductServiceImp productServiceImp;
    private final UserRepository userRepository;
    private final OrderServiceImpl orderService;
    @Autowired
    public WarehouseController(UserService userService, WarehouseEconomicService warehouseEconomicService, WarehouseAssignmentService warehouseAssignmentService, ProductServiceImp productServiceImp, UserRepository userRepository, OrderServiceImpl orderService) {
        this.userService = userService;
        this.warehouseEconomicService = warehouseEconomicService;
        this.warehouseAssignmentService = warehouseAssignmentService;
        this.productServiceImp = productServiceImp;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }
    @GetMapping("/user")
    public Page<UserResponse> getUsersByRole(String roleName,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllByRole(roleName, pageable)
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getAvatar(),
                        u.getRegistrationDate()
                ));
    }
    @PostMapping("/warehouse/create")
    public ResponseEntity<?> createWareHouse(@RequestBody WarehouseRequest request) {
        Warehouse result = this.warehouseEconomicService.createWarehouse(request);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/warehouse/get")
    public ResponseEntity<?> getAllWareHouse() {
        return ResponseEntity.of(Optional.ofNullable(warehouseAssignmentService.getAllWarehouses()));
    }
    @GetMapping("/product/quantity")
    public ResponseEntity<?> getQuantityInStock() {
        return ResponseEntity.of(Optional.ofNullable(productServiceImp.getAllProductStock()));
    }
    @GetMapping("/dashboard/ecommerce")
    public DashboardMetricsDTO getDashboardMetrics() {
        return warehouseEconomicService.getDashboardMetrics()
                .orElse(new DashboardMetricsDTO(
                        BigDecimal.ZERO, // totalRevenue
                        0L,              // totalOrders
                        0L,              // totalCustomers
                        BigDecimal.ZERO, // orderFrequency
                        BigDecimal.ZERO, // avgOrderValue
                        BigDecimal.ZERO, // totalCost
                        BigDecimal.ZERO  // profitMargin
                ));
    }
    @PostMapping("/warehouse/assignment")
    public ResponseEntity<?> assignmentManagerWareHouse(@RequestBody assignManagerToWarehouseRequest req) {
        return ResponseEntity.ok(
                warehouseAssignmentService.assignManagerToWarehouse(
                        req.email(),
                        req.warehouseId(),
                        req.maxStaff(),
                        req.maxWarehouses()
                )
        );    }
}

