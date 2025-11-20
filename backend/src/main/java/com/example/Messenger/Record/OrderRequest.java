package com.example.Messenger.Record;

import java.util.List;

public record OrderRequest(
        String customerName,
        String customerEmail,
        String address,
        List<OrderItemRequest> items,
        String token
) {
}
