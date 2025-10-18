package com.example.Messenger.Entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(
        name = "product",
        indexes = {
                @Index(name = "idx_product_price", columnList = "price"),
                @Index(name = "idx_product_category", columnList = "category_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Product {
    @Id
    private String id;
    private LocalDate createdAt;
    private String name;
    @Column(length = 2000)


    private String description;

    private Integer quantity;
    private Double price;
    @Column(length = 5000)
    private String embedding;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Image> images = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("products")
    private Category category;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Feature> features = new HashSet<>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Discount> discounts = new HashSet<>();

    public  Product(){}
    public Product(String id, LocalDate createdAt, String name, String description, Double price, String embedding) {
        this.id = id;
        this.createdAt = createdAt;
        this.name = name;
        this.description = description;
        this.price = price;
        this.embedding = embedding;
    }
    public Double getCurrentDiscountPercentage() {
        LocalDate today = LocalDate.now();

        return discounts.stream()
                .filter(d -> d.getStartDate() != null && d.getEndDate() != null)
                .filter(d -> !today.isBefore(d.getStartDate()) && !today.isAfter(d.getEndDate()))
                .map(Discount::getPercentage)
                .max(Double::compareTo) // nếu có nhiều giảm giá trùng thời gian → lấy lớn nhất
                .orElse(0.0);
    }

    // ✅ Hàm tính giá hiện tại sau khi áp giảm giá
    public Double getCurrentPrice() {
        Double discount = getCurrentDiscountPercentage();
        return price * (1 - discount);
    }

    // ✅ Tiện ích thêm / xoá discount
    public void addDiscount(Discount discount) {
        discounts.add(discount);
        discount.setProduct(this);
    }

    public void removeDiscount(Discount discount) {
        discounts.remove(discount);
        discount.setProduct(null);
    }

    // Getters / Setters
    public Set<Discount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(Set<Discount> discounts) {
        this.discounts = discounts;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(Set<Feature> features) {
        this.features = features;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
