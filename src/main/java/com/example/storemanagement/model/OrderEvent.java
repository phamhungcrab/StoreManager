package com.example.storemanagement.model;

import java.time.LocalDateTime;

/** Lưu lịch sử sự kiện đơn hàng (theo dõi lifecycle) */
public class OrderEvent {
    private Long id;
    private Long orderId;
    private String eventType; // CREATED / CONFIRMED / PAID / CANCELED / REFUNDED / NOTE
    private String dataJson;  // payload tuỳ chọn (JSON text)
    private String note;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}