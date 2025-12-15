package com.example.Messenger.Record;

import com.example.Messenger.Entity.Category;
import com.example.Messenger.Entity.Feature;

import java.util.List;

public record ProductEmbedding (
        String id,
        String name,
        String description,
        Category category,
        List<Feature> features
) {}

