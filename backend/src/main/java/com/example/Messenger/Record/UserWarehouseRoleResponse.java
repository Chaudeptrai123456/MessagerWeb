package com.example.Messenger.Record;

public record UserWarehouseRoleResponse(
        Long id,
        String userId,
        String userEmail,
        String warehouseId,
        String warehouseName,
        WarehouseRole role,
        Integer maxStaff,
        Integer maxWarehouses
) {}
