package com.example.Messenger.Repository;

import com.example.Messenger.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {}
