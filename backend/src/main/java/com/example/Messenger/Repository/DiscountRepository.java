package com.example.Messenger.Repository;

import com.example.Messenger.Entity.Discount;
import com.example.Messenger.Entity.Product;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, String> {
    @Modifying
    @Transactional
    @Query("delete from Discount d where d.endDate <= :today")
    int deleteExpiredDiscounts(@Param("today") LocalDate today);
    @Query("""
        SELECT d.product 
        FROM Discount d
        WHERE d.endDate >= :today
        ORDER BY d.percentage DESC
        """)
    List<Product> findTopDiscountProducts(@Param("today") LocalDate today, Pageable pageable);
}
