package com.example.Messenger.Record.Request;

import java.util.List;

public record OrderRequest(
        String customerName,
        String customerEmail,
        String address,
        List<OrderItemRequest> items,
        String token
) {
}
