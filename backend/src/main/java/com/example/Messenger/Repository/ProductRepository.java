package com.example.Messenger.Repository;

import com.example.Messenger.Entity.Product;


import org.springframework.data.jpa.repository.JpaRepository;
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
}
