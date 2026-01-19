package com.example.Messenger.Service.Implement;

import com.example.Messenger.Entity.Warehouse;
import com.example.Messenger.Record.DTO.DashboardMetricsDTO;
import com.example.Messenger.Record.DTO.WarehouseEconomicDTO;
import com.example.Messenger.Record.Request.WarehouseRequest;
import com.example.Messenger.Record.View.DashboardMetricsView;
import com.example.Messenger.Record.View.ProductInStockView;
import com.example.Messenger.Repository.InventoryLogRepository;
import com.example.Messenger.Repository.OrderRepository;
import com.example.Messenger.Repository.StockImportRepository;
import com.example.Messenger.Repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional()
public class WarehouseEconomicService {

    private final InventoryLogRepository inventoryLogRepository;
    private final StockImportRepository stockImportRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderRepository orderRepository;
    @Autowired
    public WarehouseEconomicService(InventoryLogRepository inventoryLogRepository, StockImportRepository stockImportRepository, WarehouseRepository warehouseRepository, OrderRepository orderRepository) {
        this.inventoryLogRepository = inventoryLogRepository;
        this.stockImportRepository = stockImportRepository;
        this.warehouseRepository = warehouseRepository;
        this.orderRepository = orderRepository;
    }
    public List<ProductInStockView> getAllProductInAllWarehouse(){
        return this.warehouseRepository.findAllProductInWareHouse();
    }
    public Warehouse createWarehouse(WarehouseRequest req) {
        return warehouseRepository.findByName(req.name())
                .orElseGet(() -> {
                    Warehouse warehouse = new Warehouse();
                    warehouse.setId(UUID.randomUUID().toString());
                    warehouse.setName(req.name());
                    warehouse.setLocation(req.location());
                    return warehouseRepository.save(warehouse);
                });
    }
    public Optional<DashboardMetricsView> getDashboardMetrics() {
        return orderRepository.findWarehousesWithEnoughStock();
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