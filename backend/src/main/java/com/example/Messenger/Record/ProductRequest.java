package com.example.Messenger.Record;

import java.util.List;

public record ProductRequest(
        String name,
        String description,
        double price,
        int quantity,
        String categoryId,
        List<String> images,
        List<String> features
) {}
