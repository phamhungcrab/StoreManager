package com.example.storemanagement.model;

import java.time.LocalDateTime;

public class Customer {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private Integer points;
    private LocalDateTime createdAt;

    public Customer() {}

    public Customer(Long id, String name, String phone, String email, Integer points, LocalDateTime createdAt) {
        this.id = id; this.name = name; this.phone = phone; this.email = email; this.points = points; this.createdAt = createdAt;
    }

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}