package com.example.Messenger.Repository;

import com.example.Messenger.Entity.StockImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockImportRepository  extends JpaRepository<StockImport, Long> {
}
