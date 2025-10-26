package com.example.storemanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order – đại diện cho bảng orders
 * Lưu ý: totalAmount (DB) là SUBTOTAL (tổng dòng items, do trigger cập nhật).
 * Discount được lưu ở cột riêng; Grand Total = subtotal - discount (>=0) tính ở tầng Service/UI.
 */
public class Order {
    private Long id;
    private String orderCode;
    private Long storeId;
    private Long customerId; // có thể null
    private BigDecimal totalAmount; // SUBTOTAL do trigger tính
    private BigDecimal discount;    // giảm giá của đơn (0 nếu không có)
    private Status status;          // CONFIRMED/PAID/CANCELED/REFUNDED
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public enum Status { CONFIRMED, PAID, CANCELED, REFUNDED }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}