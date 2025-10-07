package com.example.Messenger.Record;

public record OrderItemRequest(
        String productId,
        Integer quantity
) {
}
