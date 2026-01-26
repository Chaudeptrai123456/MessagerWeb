package com.example.Messenger.Record.Orther;

public class ChurnRisk {
    private String customerEmail;
    private String lastOrderAt;
    private int ordersLast30Days;
    private double recencyScore;
    private double frequencyScore;
    private double churnRisk;
    private String churnLabel;

    public ChurnRisk(String customerEmail, String lastOrderAt, int ordersLast30Days,
                     double recencyScore, double frequencyScore, double churnRisk,
                     String churnLabel) {
        this.customerEmail = customerEmail;
        this.lastOrderAt = lastOrderAt;
        this.ordersLast30Days = ordersLast30Days;
        this.recencyScore = recencyScore;
        this.frequencyScore = frequencyScore;
        this.churnRisk = churnRisk;
        this.churnLabel = churnLabel;
    }

    // getters
    public String getCustomerEmail() { return customerEmail; }
    public String getLastOrderAt() { return lastOrderAt; }
    public int getOrdersLast30Days() { return ordersLast30Days; }
    public double getRecencyScore() { return recencyScore; }
    public double getFrequencyScore() { return frequencyScore; }
    public double getChurnRisk() { return churnRisk; }
    public String getChurnLabel() { return churnLabel; }
}

