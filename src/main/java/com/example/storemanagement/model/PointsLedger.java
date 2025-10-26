package com.example.storemanagement.model;

import java.time.LocalDateTime;

/** Ghi lại mọi biến động điểm của khách hàng */
public class PointsLedger {
    private Long id;
    private Long customerId;
    private Long orderId;      // có thể null (điều chỉnh thủ công)
    private Integer delta;     // + hoặc -
    private String reason;     // PURCHASE / REFUND / MANUAL_ADJUST
    private String note;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getDelta() { return delta; }
    public void setDelta(Integer delta) { this.delta = delta; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}