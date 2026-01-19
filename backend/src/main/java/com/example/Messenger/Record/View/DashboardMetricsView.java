package com.example.Messenger.Record.View;

import java.math.BigDecimal;

public interface DashboardMetricsView {

    BigDecimal getTotalRevenue();
    Long getTotalOrders();
    Long getTotalCustomers();
    BigDecimal getOrderFrequency();
    BigDecimal getAvgOrderValue();
    BigDecimal getTotalCost();
    BigDecimal getProfitMargin();
}