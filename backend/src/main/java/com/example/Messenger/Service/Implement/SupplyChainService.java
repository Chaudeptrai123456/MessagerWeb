package com.example.Messenger.Service.Implement;

import com.example.Messenger.Repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplyChainService {

    private final InventoryLogRepository inventoryLogRepository;

    public SupplyChainService(InventoryLogRepository inventoryLogRepository) {
        this.inventoryLogRepository = inventoryLogRepository;
    }

    public Map<String, Integer> stockByWarehouse(String productId) {

        List<Object[]> rows = inventoryLogRepository.sumByWarehouse(productId);

        return rows.stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],   // warehouseId
                        r -> ((Number) r[1]).intValue()
                ));
    }
}