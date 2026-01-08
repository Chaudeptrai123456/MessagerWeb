package com.example.Messenger.Record;

public record assignManagerToWarehouseRequest (
        String email,
        String warehouseId,
        Integer maxStaff,
        Integer maxWarehouses
) {
}
