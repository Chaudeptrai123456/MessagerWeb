package com.example.Messenger.Repository;

import com.example.Messenger.Entity.InventoryLog;
import com.example.Messenger.Entity.Product;
import com.example.Messenger.Record.InventoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    List<InventoryLog> findByRefId(String refId);

    boolean existsByProductIdAndRefIdAndType(
            String productId,
            String refId,
            InventoryType type
    );
    boolean existsByProductAndRefIdAndType(
            Product product,
            String refId,
            InventoryType type
    );
}
