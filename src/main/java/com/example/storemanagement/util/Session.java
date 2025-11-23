// File: Session.java
package com.example.storemanagement.util;

public class Session {
    private static String username;
    private static String role;
    private static Long storeId;

    public static void setUser(String user, String userRole, Long userStoreId) {
        username = user;
        role = userRole;
        storeId = userStoreId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static Long getStoreId() {
        return storeId;
    }

    public static void clear() {
        username = null;
        role = null;
        storeId = null;
    }
}
