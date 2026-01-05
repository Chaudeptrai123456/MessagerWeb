package com.example.Messenger.Controller;

import com.example.Messenger.Entity.Product;
import com.example.Messenger.Entity.StockImport;
import com.example.Messenger.Record.AdjustStockRequest;
import com.example.Messenger.Record.StockImportRequest;
import com.example.Messenger.Repository.InventoryLogRepository;
import com.example.Messenger.Repository.ProductRepository;
import com.example.Messenger.Repository.StockImportRepository;
import com.example.Messenger.Service.Implement.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryAdminController {

    private final StockImportRepository stockImportRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final InventoryLogRepository inventoryLogRepository;
    @Autowired
    public InventoryAdminController(StockImportRepository stockImportRepository, ProductRepository productRepository, InventoryService inventoryService, InventoryLogRepository inventoryLogRepository) {
        this.stockImportRepository = stockImportRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.inventoryLogRepository = inventoryLogRepository;
    }

    /**
     * =========================
     * 1️⃣ TẠO PHIẾU NHẬP KHO
     * =========================
     */
    @PostMapping("/stock-import")
    public ResponseEntity<?> createStockImport(
            @RequestBody StockImportRequest request
    ) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        StockImport stockImport = new StockImport();
        stockImport.setProduct(product);
        stockImport.setQuantity(request.getQuantity());
        stockImport.setImportPrice(request.getImportPrice());
        stockImport.setSupplier(request.getSupplier());
        stockImport.setNote(request.getNote());

        stockImportRepository.save(stockImport);

        return ResponseEntity.ok(stockImport);
    }

    /**
     * =========================
     * 2️⃣ XÁC NHẬN NHẬP KHO
     * =========================
     */
    @PostMapping("/stock-import/{id}/confirm")
    public ResponseEntity<?> confirmStockImport(
            @PathVariable Long id
    ) {
        inventoryService.importFromStockImport(id);
        return ResponseEntity.ok("Stock imported successfully");
    }

    /**
     * =========================
     * 3️⃣ ADMIN CHỈNH KHO
     * =========================
     */
    @PostMapping("/product/{productId}/adjust")
    public ResponseEntity<?> adjustStock(
            @PathVariable String productId,
            @RequestBody AdjustStockRequest request
    ) {
        inventoryService.adjustStock(
                productId,
                request.getNewQuantity(),
                request.getReason()
        );
        return ResponseEntity.ok("Stock adjusted");
    }

    /**
     * =========================
     * 4️⃣ XEM LỊCH SỬ KHO
     * =========================
     */
    @GetMapping("/product/{productId}/logs")
    public ResponseEntity<?> inventoryLogs(
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(
                inventoryLogRepository.findAll().stream()
                        .filter(log -> log.getProduct().getId().equals(productId))
                        .toList()
        );
    }
}
