package com.example.Messenger.Repository;

import com.example.Messenger.Entity.User;
import com.example.Messenger.Entity.UserWarehouseRole;
import com.example.Messenger.Entity.Warehouse;
import com.example.Messenger.Record.WarehouseRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWarehouseRoleRepository  extends JpaRepository<UserWarehouseRole, Long> {

    boolean existsByUserAndWarehouse(User user, Warehouse warehouse);
    boolean existsByUserIdAndWarehouseIdAndRole(
            String userId,
            String warehouseId,
            WarehouseRole role
    );
    Optional<UserWarehouseRole> findByUserIdAndWarehouseId(String userId,  String warehouseId );
}