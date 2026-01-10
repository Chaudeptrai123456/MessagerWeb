package com.example.Messenger.Record;

import com.example.Messenger.Entity.Category;
import com.example.Messenger.Entity.Discount;
import com.example.Messenger.Entity.Feature;
import com.example.Messenger.Entity.Image;

import java.time.LocalDate;
import java.util.Set;

public record ProductResponseDTO(
        String id,
        LocalDate createdAt,
        LocalDate updateAt,
        String name,
        String description,
        Integer quantity,          // 👈 quantity fake
        Double price,
        String embedding,
        Set<Image> images,
        Category category,
        Set<Feature> features,
        Set<Discount> discounts,
        Double currentPrice,
        Integer currentDiscountPercentage
) {}
