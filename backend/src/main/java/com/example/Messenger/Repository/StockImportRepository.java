package com.example.Messenger.Repository;

import com.example.Messenger.Entity.StockImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface StockImportRepository  extends JpaRepository<StockImport, Long> {

    @Query("""
        SELECT COALESCE(SUM(si.quantity * si.importPrice), 0)
        FROM StockImport si
        WHERE si.product.warehouse.id = :warehouseId
          AND si.createdAt BETWEEN :from AND :to
    """)
    double totalImportCost(
            String warehouseId,
            LocalDateTime from,
            LocalDateTime to
    );
}
