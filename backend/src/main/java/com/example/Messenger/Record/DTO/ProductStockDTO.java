package com.example.Messenger.Record.DTO;

public record ProductStockDTO(
        String productId,
        String productName,
        String warehouseId,
        String warehouseName,
        Integer quantity
) {}