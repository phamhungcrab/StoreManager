package com.example.storemanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private Long id;
    private Long supplierId;
    private String sku;
    private String name;
    private String unit;
    private BigDecimal unitPrice; // giá bán đề xuất
    private BigDecimal unitCost;  // giá vốn
    private boolean active;
    private LocalDateTime createdAt;

    public Product() {}

    public Product(Long id, Long supplierId, String sku, String name, String unit,
                   BigDecimal unitPrice, BigDecimal unitCost, boolean active, LocalDateTime createdAt) {
        this.id = id; this.supplierId = supplierId; this.sku = sku; this.name = name; this.unit = unit;
        this.unitPrice = unitPrice; this.unitCost = unitCost; this.active = active; this.createdAt = createdAt;
    }

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}