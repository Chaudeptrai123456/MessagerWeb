package com.example.Messenger.Entity;

import com.example.Messenger.Record.InventoryType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@Table(
        name = "inventory_log"
//        uniqueConstraints = @UniqueConstraint(
//                columnNames = {"product_id", "ref_id", "type"}
//        )
)
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryType type;

    @Column(nullable = false)
    private Integer quantity; // + nhập, - xuất

    private Double unitPrice;

    @Column(name = "ref_id")
    private String refId;

    private LocalDateTime createdAt = LocalDateTime.now();

    public InventoryLog() {}

    public InventoryLog(Product product,
                        InventoryType type,
                        Integer quantity,
                        Double unitPrice,
                        String refId) {
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.refId = refId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public InventoryType getType() {
        return type;
    }

    public void setType(InventoryType type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
