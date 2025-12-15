package com.example.Messenger.Service;

import com.example.Messenger.Entity.Category;
import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);
    Category updateCategory(String id, Category category);
    Category getCategoryById(String id);
    List<Category> getAllCategories();
    void deleteCategory(String id);
}