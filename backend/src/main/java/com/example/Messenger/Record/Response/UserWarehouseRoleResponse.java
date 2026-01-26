package com.example.Messenger.Record.Response;

import com.example.Messenger.Record.Type.WarehouseRole;

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
