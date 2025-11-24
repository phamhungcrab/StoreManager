package com.example.storemanagement.dao;

import com.example.storemanagement.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.username, u.email, u.role, u.store_id, s.name as store_name " +
                "FROM users u LEFT JOIN stores s ON u.store_id = s.id ORDER BY u.username";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Long storeId = rs.getObject("store_id") != null ? rs.getLong("store_id") : null;
                String storeName = rs.getString("store_name");
                users.add(new User(
                        rs.getString("username"),
                        "", // password hash hidden
                        rs.getString("email"),
                        rs.getString("role"),
                        storeId,
                        storeName));
            }
        }
        return users;
    }

    public boolean updateUserRole(String username, String newRole) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE username = ?";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean assignStore(String username, Long storeId) throws SQLException {
        String sql = "UPDATE users SET store_id = ? WHERE username = ?";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            if (storeId == null) {
                ps.setNull(1, java.sql.Types.BIGINT);
            } else {
                ps.setLong(1, storeId);
            }
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT u.username, u.password, u.email, u.role, u.store_id, s.name as store_name " +
                "FROM users u LEFT JOIN stores s ON u.store_id = s.id WHERE u.username = ?";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long storeId = rs.getObject("store_id") != null ? rs.getLong("store_id") : null;
                    String storeName = rs.getString("store_name");
                    return new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("role"),
                            storeId,
                            storeName);
                }
            }
        }
        return null;
    }
}
