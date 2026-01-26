package com.example.Messenger.Repository;

import com.example.Messenger.Entity.StockImport;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
    @Query(    nativeQuery = true
            ,value="""
SELECT si.import_price
FROM stock_import si
WHERE si.product_id = :productId
  AND si.warehouse_id = :warehouseId
ORDER BY si.imported_at DESC
LIMIT 1;
""")
    Double findLatestImportPrice(
            @Param("productId") String productId,
            @Param("warehouseId") String warehouseId
    );
}
