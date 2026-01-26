package com.example.Messenger.Service.Implement;

import com.example.Messenger.Entity.Product;
import com.example.Messenger.Record.Orther.EconomicReport;
import com.example.Messenger.Record.Type.InventoryType;
import com.example.Messenger.Repository.InventoryLogRepository;
import com.example.Messenger.Repository.ProductRepository;
import com.example.Messenger.Repository.StockImportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EconomicService {

    private final InventoryLogRepository inventoryLogRepository;
    private final StockImportRepository stockImportRepository;
    private final ProductRepository productRepository;

    public EconomicService(InventoryLogRepository inventoryLogRepository, StockImportRepository stockImportRepository, ProductRepository productRepository) {
        this.inventoryLogRepository = inventoryLogRepository;
        this.stockImportRepository = stockImportRepository;
        this.productRepository = productRepository;
    }
    @Transactional()
    public EconomicReport calculate(
            String productId,
            LocalDate from,
            LocalDate to
    ) {

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);

        // 1️⃣ Nhập kho
        int importedQty = inventoryLogRepository
                .sumQuantityByType(InventoryType.IMPORT, start, end);
        double importCost = stockImportRepository
                .totalImportCost(productId, start, end);
        // 2️⃣ Bán hàng
        int soldQty = Math.abs(
                inventoryLogRepository
                        .sumQuantityByType(InventoryType.SALE, start, end)
        );
        double revenue = inventoryLogRepository
                .sumMoneyByType(InventoryType.SALE, start, end);
        // 3️⃣ Kho hiện tại
        Product product = productRepository.findById(productId).orElseThrow();
        int currentStock = product.getQuantity();
        // 4️⃣ Giá trị kho (avg import)
        double avgImportPrice = importedQty == 0 ? 0 : importCost / importedQty;
        double stockValue = currentStock * avgImportPrice;
        // 5️⃣ Lợi nhuận
        double profit = revenue - importCost;
        return new EconomicReport(
                importedQty,
                importCost,
                soldQty,
                revenue,
                profit,
                currentStock,
                stockValue
        );
    }
}