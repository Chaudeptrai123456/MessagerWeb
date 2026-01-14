package com.example.Messenger.Record;

public class ProductInStock {
    private String id;
    private String name;
    private Double price;
    private String description;
    private Long totalQuantity;

    public ProductInStock(String id, String name, Double price, String description, Long totalQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.totalQuantity = totalQuantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
 
}
