package com.example.Messenger.Controller;

import com.example.Messenger.Record.DTO.WarehouseEconomicDTO;
import com.example.Messenger.Service.Implement.WarehouseEconomicService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/warehouse")
@RequiredArgsConstructor
public class WarehouseEconomicController {

    private final WarehouseEconomicService warehouseEconomicService;
    @Autowired
    public WarehouseEconomicController(WarehouseEconomicService warehouseEconomicService) {
        this.warehouseEconomicService = warehouseEconomicService;
    }

    @GetMapping("/{warehouseId}/economic")
    public ResponseEntity<WarehouseEconomicDTO> economic(
            @PathVariable String warehouseId,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to
    ) {
        return ResponseEntity.ok(
                warehouseEconomicService.calculateWarehouseProfit(
                        warehouseId, from, to
                )
        );
    }
}