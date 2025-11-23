package com.example.storemanagement.model;

public class User {
    // private int id;
    private String username;
    private String passwordHash;
    private String email;
    private String role;
    private Long storeId; // Nullable if admin/logistic/accountant

    public User(String username, String passwordHash, String email, String role, Long storeId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
        this.storeId = storeId;
    }

    // Getters & setters
    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
}
