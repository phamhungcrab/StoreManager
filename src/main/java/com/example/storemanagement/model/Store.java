package com.example.storemanagement.model;

import java.time.LocalDateTime;

public class Store {
    private Long id;
    private String code;
    private String name;
    private String type; // CENTRAL / RETAIL
    private String address;
    private String phone;
    private LocalDateTime createdAt;

    public Store() {
    }

    public Store(Long id, String code, String name, String type, String address, String phone,
            LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.address = address;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return (name == null ? "Store" : name) + (code != null ? " (" + code + ")" : "")
                + (type != null ? " [" + type + "]" : "");
    }
}