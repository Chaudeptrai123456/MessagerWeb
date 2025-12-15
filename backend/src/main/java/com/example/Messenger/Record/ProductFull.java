package com.example.Messenger.Record;

import lombok.Data;
import java.util.List;

@Data
public class ProductFull {
    private String id;
    private String createdAt;
    private String name;
    private String description;
    private int quantity;
    private double price;
    private double currentDiscountPercentage;
    private double currentPrice;
    private List<Image> images;
    private Category category;
    private List<Feature> features;
    private List<Discount> discounts;

    @Data
    public static class Image {
        private int id;
        private String filename;
        private String contentType;
        private String url;
    }

    @Data
    public static class Category {
        private String id;
        private String name;
        private String description;
    }

    @Data
    public static class Feature {
        private int id;
        private String name;
        private String value;
        private String product;
    }

    @Data
    public static class Discount {
        private String id;
        private double percentage;
        private String startDate;
        private String endDate;
        private String product;
    }
}
