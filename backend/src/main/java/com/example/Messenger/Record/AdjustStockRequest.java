package com.example.Messenger.Record;

import lombok.Data;

@Data
public class AdjustStockRequest {
    private Integer newQuantity;
    private String reason;

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
