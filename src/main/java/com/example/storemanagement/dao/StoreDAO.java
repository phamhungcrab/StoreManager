package com.example.storemanagement.dao;

import com.example.storemanagement.model.Store;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StoreDAO {

    public List<Store> getAllStores() throws SQLException {
        List<Store> stores = new ArrayList<>();
        String sql = "SELECT id, code, name, type, address, phone, created_at FROM stores ORDER BY name";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Store s = new Store();
                s.setId(rs.getLong("id"));
                s.setCode(rs.getString("code"));
                s.setName(rs.getString("name"));
                s.setType(rs.getString("type"));
                s.setAddress(rs.getString("address"));
                s.setPhone(rs.getString("phone"));
                if (rs.getTimestamp("created_at") != null) {
                    s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
                stores.add(s);
            }
        }
        return stores;
    }
}
