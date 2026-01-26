package com.example.Messenger.Record.Request;

public record assignManagerToWarehouseRequest (
        String email,
        String warehouseId,
        Integer maxStaff,
        Integer maxWarehouses
) {
}
