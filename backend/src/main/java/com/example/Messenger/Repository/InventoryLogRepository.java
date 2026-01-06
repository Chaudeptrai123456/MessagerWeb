package com.example.Messenger.Repository;

import com.example.Messenger.Entity.InventoryLog;
import com.example.Messenger.Entity.Product;
import com.example.Messenger.Record.InventoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
    @Query("""
        SELECT COALESCE(SUM(-il.quantity), 0)
        FROM InventoryLog il
        WHERE il.type = 'SALE'
          AND il.product.warehouse.id = :warehouseId
          AND il.createdAt BETWEEN :from AND :to
    """)
    long totalSoldQuantity(
            String warehouseId,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("""
        SELECT COALESCE(SUM(-il.quantity * il.unitPrice), 0)
        FROM InventoryLog il
        WHERE il.type = 'SALE'
          AND il.product.warehouse.id = :warehouseId
          AND il.createdAt BETWEEN :from AND :to
    """)
    double totalRevenue(
            String warehouseId,
            LocalDateTime from,
            LocalDateTime to
    );
    @Query("""
    SELECT l.warehouse.id, SUM(l.quantity)
    FROM InventoryLog l
    WHERE l.product.id = :productId
    GROUP BY l.warehouse.id
""")
    List<Object[]> sumByWarehouse(String productId);

    @Query("""
        SELECT COALESCE(SUM(l.quantity), 0)
        FROM InventoryLog l
        WHERE l.product.id = :productId AND l.warehouse.id = :warehouseId
    """)
    int currentStock(
            String productId,
            String warehouseId
    );
    @Query("""
        SELECT l
        FROM InventoryLog l
        WHERE l.product.id = :productId AND l.quantity < 0
        ORDER BY l.createdAt DESC
    """)
    List<InventoryLog> exportHistory(String productId);
    List<InventoryLog> findByRefId(String refId);
    @Query("""
        SELECT COALESCE(SUM(l.quantity), 0)
        FROM InventoryLog l
        WHERE l.type = :type
          AND l.createdAt BETWEEN :from AND :to
    """)
    int sumQuantityByType(
            InventoryType type,
            LocalDateTime from,
            LocalDateTime to
    );
    @Query("""
        SELECT COALESCE(SUM(ABS(l.quantity) * l.unitPrice), 0)
        FROM InventoryLog l
        WHERE l.type = :type
          AND l.createdAt BETWEEN :from AND :to
    """)
    double sumMoneyByType(
            InventoryType type,
            LocalDateTime from,
            LocalDateTime to
    );
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
