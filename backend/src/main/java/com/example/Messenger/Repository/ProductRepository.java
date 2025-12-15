package com.example.Messenger.Repository;

import com.example.Messenger.Entity.Product;


import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    // Tìm theo tên product
    List<Product> findByNameContainingIgnoreCase(String name);

    // Tìm theo category
    List<Product> findByCategoryName(String categoryName);

    // Tìm theo feature
    List<Product> findByFeaturesNameContainingIgnoreCase(String featureName);
    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN p.features f
    WHERE 
        (:categoryId IS NOT NULL AND p.category.id = :categoryId)
        OR (:minPrice IS NOT NULL AND p.price >= :minPrice)
        OR (:maxPrice IS NOT NULL AND p.price <= :maxPrice)
        OR (:featureName IS NOT NULL AND LOWER(f.name) LIKE LOWER(CONCAT('%', :featureName, '%')))
        OR (:featureValue IS NOT NULL AND LOWER(f.value) LIKE LOWER(CONCAT('%', :featureValue, '%')))
    """)
    Page<Product> searchProducts(
            String categoryId,
            Double minPrice,
            Double maxPrice,
            String featureName,
            String featureValue,
            Pageable pageable
    );
}
