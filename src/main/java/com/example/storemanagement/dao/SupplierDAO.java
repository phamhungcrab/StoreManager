package com.example.storemanagement.dao;

import com.example.storemanagement.model.Supplier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> getAll() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY name";
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
                Timestamp ts = rs.getTimestamp("created_at");
                s.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
                list.add(s);
            }
        }
        return list;
    }
}
