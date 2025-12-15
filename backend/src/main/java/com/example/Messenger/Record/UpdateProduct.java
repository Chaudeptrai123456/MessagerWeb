package com.example.Messenger.Record;

import com.example.Messenger.Entity.Feature;
import jakarta.persistence.Column;

import java.util.HashSet;
import java.util.Set;

public class UpdateProduct {
    private String name;

    private String description;

    private Integer quantity;
    private Double price;

    private Set<Feature> features = new HashSet<>();

    public Set<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(Set<Feature> features) {
        this.features = features;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}

