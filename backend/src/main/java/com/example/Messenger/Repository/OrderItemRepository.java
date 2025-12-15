package com.example.Messenger.Repository;

import com.example.Messenger.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository  extends JpaRepository<OrderItem, Long> {
}
