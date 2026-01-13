package com.example.Messenger.Controller.Staff;

import com.example.Messenger.Record.ImportStockRequest;
import com.example.Messenger.Service.Implement.SupplyChainService;
import com.example.Messenger.Service.Implement.UserService;
import com.example.Messenger.Service.Implement.WarehouseEconomicService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/staff")
public class ManageWarehouse {
    private final WarehouseEconomicService warehouseEconomicService;
    private final SupplyChainService supplyChainService;
    public ManageWarehouse(WarehouseEconomicService warehouseEconomicService, SupplyChainService supplyChainService) {
        this.warehouseEconomicService = warehouseEconomicService;
        this.supplyChainService = supplyChainService;
    }
    @PostMapping("/warehouses/{warehouseId}/import")
    @PreAuthorize("@warehousePermissionService.hasManagerPermission(#warehouseId)")
    public ResponseEntity<?> importStock(
            @PathVariable String warehouseId,
            @RequestBody ImportStockRequest req
    ) {
        supplyChainService.importStock(
                warehouseId,
                req.productId(),
                req.quantity(),
                req.importPrice(),
                req.supplier(),
                req.note()
        );
        return ResponseEntity.ok("Imported successfully");
    }


}
