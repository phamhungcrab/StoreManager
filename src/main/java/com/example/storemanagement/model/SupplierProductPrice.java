package com.example.storemanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SupplierProductPrice {
    private Long id;
    private Long productId;
    private Long supplierId;
    private BigDecimal importPrice;
    private LocalDateTime updatedAt;

    // Display fields (not in table)
    private String productName;
    private String sku;
    private String supplierName;

    public SupplierProductPrice() {
    }

    public SupplierProductPrice(Long id, Long productId, Long supplierId, BigDecimal importPrice,
            LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.supplierId = supplierId;
        this.importPrice = importPrice;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}
