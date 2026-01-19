package com.example.Messenger.Record.Request;
public record ImportStockRequest(
        String productId,
        Integer quantity,
        Double importPrice,
        String supplier,
        String note
) {}
