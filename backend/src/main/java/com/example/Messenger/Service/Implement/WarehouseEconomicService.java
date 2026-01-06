package com.example.Messenger.Service.Implement;

import com.example.Messenger.Record.WarehouseEconomicDTO;
import com.example.Messenger.Repository.InventoryLogRepository;
import com.example.Messenger.Repository.StockImportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional()
public class WarehouseEconomicService {

    private final InventoryLogRepository inventoryLogRepository;
    private final StockImportRepository stockImportRepository;

    public WarehouseEconomicService(InventoryLogRepository inventoryLogRepository, StockImportRepository stockImportRepository) {
        this.inventoryLogRepository = inventoryLogRepository;
        this.stockImportRepository = stockImportRepository;
    }

    public WarehouseEconomicDTO calculateWarehouseProfit(
            String warehouseId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        long soldQty = inventoryLogRepository
                .totalSoldQuantity(warehouseId, from, to);

        double revenue = inventoryLogRepository
                .totalRevenue(warehouseId, from, to);

        double cost = stockImportRepository
                .totalImportCost(warehouseId, from, to);

        double profit = revenue - cost;

        return new WarehouseEconomicDTO(
                warehouseId,
                soldQty,
                revenue,
                cost,
                profit
        );
    }
}