package com.example.Messenger.Repository;

import com.example.Messenger.Entity.Authority;
import com.example.Messenger.Entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {

    Optional<Warehouse> findByName(String name);

}
