package com.example.Messenger.Record.DTO;

public record WarehouseEconomicDTO(
        String warehouseId,
        long totalSoldQuantity,
        double revenue,
        double cost,
        double profit
) {}
