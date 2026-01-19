package com.example.Messenger.Record.Request;

public record OrderItemRequest(
        String productId,
        Integer quantity
) {
}
