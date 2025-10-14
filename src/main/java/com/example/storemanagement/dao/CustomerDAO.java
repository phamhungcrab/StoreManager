package com.example.storemanagement.dao;

/*
 * CustomerDAO – CRUD + tìm kiếm/paginate cho customers.
 * Mapping theo bảng customers trong init.sql
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.storemanagement.model.Customer;

public class CustomerDAO {

    public long insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customers(name, phone, email, points) VALUES(?,?,?,?)";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setInt(4, c.getPoints() == null ? 0 : c.getPoints());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1L;
    }

    public boolean update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET name=?, phone=?, email=?, points=? WHERE id=?";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setInt(4, c.getPoints() == null ? 0 : c.getPoints());
            ps.setLong(5, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id=?";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Optional<Customer> findById(long id) throws SQLException {
        String sql = "SELECT id, name, phone, email, points, created_at FROM customers WHERE id=?";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    /**
     * Tìm kiếm theo keyword (name/phone/email) + phân trang.
     */
    public List<Customer> search(String keyword, int page, int pageSize) throws SQLException {
        if (page < 1) page = 1; if (pageSize <= 0) pageSize = 20;
        int offset = (page - 1) * pageSize;

        String base = "SELECT id, name, phone, email, points, created_at FROM customers ";
        String where = "";
        boolean hasKw = keyword != null && !keyword.isBlank();
        if (hasKw) {
            where = "WHERE (name LIKE ? OR phone LIKE ? OR email LIKE ?) ";
        }
        String tail = "ORDER BY created_at DESC LIMIT ? OFFSET ?";

        List<Customer> list = new ArrayList<>();
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(base + where + tail)) {
            int i = 1;
            if (hasKw) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(i++, kw); ps.setString(i++, kw); ps.setString(i++, kw);
            }
            ps.setInt(i++, pageSize);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int count(String keyword) throws SQLException {
        String base = "SELECT COUNT(*) FROM customers ";
        String where = "";
        boolean hasKw = keyword != null && !keyword.isBlank();
        if (hasKw) where = "WHERE (name LIKE ? OR phone LIKE ? OR email LIKE ?)";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(base + where)) {
            if (hasKw) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            }
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1);}
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setPoints(rs.getInt("points"));
        Timestamp ts = rs.getTimestamp("created_at");
        c.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return c;
    }
}