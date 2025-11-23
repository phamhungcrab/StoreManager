package com.example.storemanagement.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.storemanagement.dao.DBConnection;
import com.example.storemanagement.model.Supplier;

/**
 * Simple service for fetching suppliers from database.
 */
public class SupplierService {

    public List<Supplier> getAllSuppliers() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT id, name, contact, phone, email, address, created_at FROM suppliers ORDER BY name";

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Supplier s = new Supplier();
                s.setId(rs.getLong("id"));
                s.setName(rs.getString("name"));
                s.setContact(rs.getString("contact"));
                s.setPhone(rs.getString("phone"));
                s.setEmail(rs.getString("email"));
                s.setAddress(rs.getString("address"));
                s.setCreatedAt(
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                suppliers.add(s);
            }
        }
        return suppliers;
    }
}
