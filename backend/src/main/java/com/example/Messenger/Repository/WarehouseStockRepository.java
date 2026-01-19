package com.example.Messenger.Repository;

import com.example.Messenger.Entity.Product;
import com.example.Messenger.Entity.Warehouse;
import com.example.Messenger.Entity.WarehouseStock;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseStockRepository extends JpaRepository<WarehouseStock,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WarehouseStock> findByWarehouseAndProduct(
            Warehouse warehouse,
            Product product
    );
    @Query("""
    SELECT ws.warehouse
    FROM WarehouseStock ws
    WHERE ws.product.id = :productId
      AND ws.quantity >= :requiredQty
    ORDER BY (ws.quantity - :requiredQty) ASC
    """)
    List<Warehouse> findWarehousesWithEnoughStock(
            @Param("productId") String productId,
            @Param("requiredQty") int requiredQty
    );
    @Query("""
    SELECT COALESCE(SUM(ws.quantity), 0)
    FROM WarehouseStock ws
    WHERE ws.warehouse = :warehouse
""")
    int sumQuantityByWarehouse(@Param("warehouse") Warehouse warehouse);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT ws
    FROM WarehouseStock ws
    WHERE ws.warehouse = :warehouse
      AND ws.product = :product
""")
    Optional<WarehouseStock> findForUpdate(
            @Param("warehouse") Warehouse warehouse,
            @Param("product") Product product
    );
    @Query("""
    SELECT ws
    FROM WarehouseStock ws
    JOIN FETCH ws.product
    JOIN FETCH ws.warehouse
""")
    List<WarehouseStock> findAllWithProductAndWarehouse();
    @Query("""
        SELECT ws
        FROM WarehouseStock ws
        JOIN FETCH ws.warehouse
        WHERE ws.product.id = :productId
    """)
    List<WarehouseStock> findAllByProductId(@Param("productId") String productId);

    @Query("""
        SELECT COALESCE(SUM(ws.quantity), 0)
        FROM WarehouseStock ws
        WHERE ws.product.id = :productId
    """)
    int sumQuantityByProductId(@Param("productId") String productId);
}
