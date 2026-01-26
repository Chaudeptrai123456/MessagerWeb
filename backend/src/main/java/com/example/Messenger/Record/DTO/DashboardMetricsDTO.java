package com.example.Messenger.Record.DTO;

import java.math.BigDecimal;

public class DashboardMetricsDTO {

    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;
    private BigDecimal orderFrequency;
    private BigDecimal avgOrderValue;
    private BigDecimal totalCost;
    private BigDecimal profitMargin;

    public DashboardMetricsDTO(BigDecimal totalRevenue, Long totalOrders, Long totalCustomers, BigDecimal orderFrequency, BigDecimal avgOrderValue, BigDecimal totalCost, BigDecimal profitMargin) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.totalCustomers = totalCustomers;
        this.orderFrequency = orderFrequency;
        this.avgOrderValue = avgOrderValue;
        this.totalCost = totalCost;
        this.profitMargin = profitMargin;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public BigDecimal getOrderFrequency() {
        return orderFrequency;
    }

    public void setOrderFrequency(BigDecimal orderFrequency) {
        this.orderFrequency = orderFrequency;
    }

    public BigDecimal getAvgOrderValue() {
        return avgOrderValue;
    }

    public void setAvgOrderValue(BigDecimal avgOrderValue) {
        this.avgOrderValue = avgOrderValue;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getProfitMargin() {
        return profitMargin;
    }

    public void setProfitMargin(BigDecimal profitMargin) {
        this.profitMargin = profitMargin;
    }
}