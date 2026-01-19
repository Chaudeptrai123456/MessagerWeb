package com.example.Messenger.Record.Orther;

public class ProductInStock {
    private String id;
    private String name;
    private Double price;
    private String description;
    private Integer totalQuantity;

    public ProductInStock(String id, String name, Double price, String description, Integer totalQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.totalQuantity = totalQuantity;
    }
}
