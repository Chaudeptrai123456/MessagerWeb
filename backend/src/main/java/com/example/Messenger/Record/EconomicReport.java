package com.example.Messenger.Record;

public record EconomicReport(
        int totalImportedQty,
        double totalImportCost,

        int totalSoldQty,
        double totalRevenue,

        double profit,
        int currentStock,
        double stockValue
) {}