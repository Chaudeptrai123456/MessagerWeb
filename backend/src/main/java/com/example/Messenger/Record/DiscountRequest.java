package com.example.Messenger.Record;

import java.time.LocalDate;

public class DiscountRequest {
    private Double percentage; // vd: 0.15 = 15%
    private LocalDate startDate;
    private LocalDate endDate;

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
