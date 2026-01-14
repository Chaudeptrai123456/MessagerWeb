package com.example.Messenger.Repository;

import com.example.Messenger.Entity.Warehouse;
import com.example.Messenger.Record.DashboardMetricsDTO;
import com.example.Messenger.Record.ProductInStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {

    Optional<Warehouse> findByName(String name);
    @Query(nativeQuery = true, value = """
    SELECT
        p.id AS id,
        p.name AS name,
        p.price AS price,
        p.description AS description,
        SUM(ws.quantity) AS totalQuantity
    FROM product p
    JOIN warehouse_stock ws
        ON p.id = ws.product_id
    GROUP BY
        p.id, p.name, p.price, p.description
    """)
    List<ProductInStock> findAllProductInWareHouse();
}