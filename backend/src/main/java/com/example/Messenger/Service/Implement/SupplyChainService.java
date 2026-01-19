package com.example.Messenger.Service.Implement;

import com.example.Messenger.Entity.Product;
import com.example.Messenger.Entity.StockImport;
import com.example.Messenger.Entity.Warehouse;
import com.example.Messenger.Entity.WarehouseStock;
import com.example.Messenger.Record.Type.InventoryType;
import com.example.Messenger.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional()
public class SupplyChainService {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockImportRepository stockImportRepository;
    private final InventoryService inventoryService;
    private final InventoryLogRepository inventoryLogRepository;
    @Autowired
    public SupplyChainService(WarehouseRepository warehouseRepository, ProductRepository productRepository, WarehouseStockRepository warehouseStockRepository, StockImportRepository stockImportRepository, InventoryService inventoryService, InventoryLogRepository inventoryLogRepository) {
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockImportRepository = stockImportRepository;
        this.inventoryService = inventoryService;
        this.inventoryLogRepository = inventoryLogRepository;
    }
    public void importStock(
            String warehouseId,
            String productId,
            Integer quantity,
            Double importPrice,
            String supplier,
            String note
    ) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        /* 1 GHI LOG NHẬP KHO */
        StockImport stockImport = new StockImport();
        stockImport.setWarehouse(warehouse);
        stockImport.setProduct(product);
        stockImport.setQuantity(quantity);
        stockImport.setImportPrice(importPrice);
        stockImport.setSupplier(supplier);
        stockImport.setNote(note);
        stockImportRepository.save(stockImport);
        inventoryService.importStock(
                productId,
                quantity,
                importPrice,
                String.valueOf(InventoryType.IMPORT),
                note,
                "IMPORT_" + productId +"_TO_"+warehouseId      // refId (idempotent)
        );

        /* 2️ UPDATE TỒN KHO */
        WarehouseStock stock = warehouseStockRepository
                .findByWarehouseAndProduct(warehouse, product)
                .orElseGet(() -> {
                    WarehouseStock ws = new WarehouseStock();
                    ws.setWarehouse(warehouse);
                    ws.setProduct(product);
                    ws.setQuantity(quantity);
                    return ws;
                });

        stock.setQuantity(stock.getQuantity() + quantity);
        warehouseStockRepository.save(stock);
    }
    public Map<String, Integer> stockByWarehouse(String productId) {

        List<Object[]> rows = inventoryLogRepository.sumByWarehouse(productId);

        return rows.stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],   // warehouseId
                        r -> ((Number) r[1]).intValue()
                ));
    }
}