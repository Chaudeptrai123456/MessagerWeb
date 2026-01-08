package com.example.Messenger.Repository;

import com.example.Messenger.Entity.Authority;
import com.example.Messenger.Entity.Warehouse;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {

    Optional<Warehouse> findByName(String name);
    @Query("""
    SELECT ws.warehouse
    FROM WarehouseStock ws
    WHERE ws.product.id = :productId
      AND ws.quantity >= :requiredQty
    ORDER BY ws.quantity DESC
""")
    List<Warehouse> findWarehousesWithEnoughStock(
            @Param("productId") String productId,
            @Param("requiredQty") int requiredQty
    );
}
