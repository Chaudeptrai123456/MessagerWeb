package com.example.Messenger.Record;

public record WarehouseEconomicDTO(
        String warehouseId,
        long totalSoldQuantity,
        double revenue,
        double cost,
        double profit
) {}
