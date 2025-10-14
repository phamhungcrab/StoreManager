package com.example.storemanagement.dao;

/*
 * ProductDAO – CRUD sản phẩm + tra cứu tổng quan tồn kho (view v_inventory_overview)
 * và thao tác nhập/xuất kho qua bảng stock_moves (trigger sẽ cập nhật inventory).
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.storemanagement.model.Product;

public class ProductDAO {

    // ===================== CRUD sản phẩm =====================
    public long insert(Product p) throws SQLException {
        String sql = "INSERT INTO products(supplier_id, sku, name, unit, unit_price, unit_cost, active) " +
                     "VALUES(?,?,?,?,?,?,?)";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (p.getSupplierId() == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, p.getSupplierId());
            ps.setString(2, p.getSku());
            ps.setString(3, p.getName());
            ps.setString(4, p.getUnit());
            ps.setBigDecimal(5, p.getUnitPrice() != null ? p.getUnitPrice() : BigDecimal.ZERO);
            ps.setBigDecimal(6, p.getUnitCost()  != null ? p.getUnitCost()  : BigDecimal.ZERO);
            ps.setBoolean(7, p.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getLong(1); }
        }
        return -1L;
    }

    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE products SET supplier_id=?, sku=?, name=?, unit=?, unit_price=?, unit_cost=?, active=? WHERE id=?";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (p.getSupplierId() == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, p.getSupplierId());
            ps.setString(2, p.getSku());
            ps.setString(3, p.getName());
            ps.setString(4, p.getUnit());
            ps.setBigDecimal(5, p.getUnitPrice() != null ? p.getUnitPrice() : BigDecimal.ZERO);
            ps.setBigDecimal(6, p.getUnitCost()  != null ? p.getUnitCost()  : BigDecimal.ZERO);
            ps.setBoolean(7, p.isActive());
            ps.setLong(8, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Optional<Product> findById(long id) throws SQLException {
        String sql = "SELECT id, supplier_id, sku, name, unit, unit_price, unit_cost, active, created_at FROM products WHERE id=?";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(map(rs)); }
            return Optional.empty();
        }
    }

    public List<Product> search(String keyword, int page, int pageSize) throws SQLException {
        if (page < 1) page = 1; if (pageSize <= 0) pageSize = 20;
        int offset = (page - 1) * pageSize;
        String base = "SELECT id, supplier_id, sku, name, unit, unit_price, unit_cost, active, created_at FROM products ";
        String where = "";
        boolean hasKw = keyword != null && !keyword.isBlank();
        if (hasKw) where = "WHERE (name LIKE ? OR sku LIKE ?) ";
        String tail = "ORDER BY created_at DESC LIMIT ? OFFSET ?";

        List<Product> list = new ArrayList<>();
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(base + where + tail)) {
            int i = 1;
            if (hasKw) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(i++, kw); ps.setString(i++, kw);
            }
            ps.setInt(i++, pageSize);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    // ===================== Tồn kho & Stock move =====================

    /**
     * Nhập kho: ghi vào stock_moves (trigger sẽ cộng tồn kho tương ứng)
     */
    public void importStock(long storeId, long productId, int quantity, String note) throws SQLException {
        String sql = "INSERT INTO stock_moves(store_id, product_id, move_type, quantity, note) VALUES (?,?,?,?,?)";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, storeId);
            ps.setLong(2, productId);
            ps.setString(3, "IMPORT");
            ps.setInt(4, quantity);
            ps.setString(5, note);
            ps.executeUpdate();
        }
    }

    /**
     * Xuất kho: ghi vào stock_moves (trigger sẽ trừ tồn kho; nếu âm → trigger chặn)
     */
    public void exportStock(long storeId, long productId, int quantity, String note) throws SQLException {
        String sql = "INSERT INTO stock_moves(store_id, product_id, move_type, quantity, note) VALUES (?,?,?,?,?)";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, storeId);
            ps.setLong(2, productId);
            ps.setString(3, "EXPORT");
            ps.setInt(4, quantity);
            ps.setString(5, note);
            ps.executeUpdate();
        }
    }

    /**
     * Tổng quan tồn kho (dùng view v_inventory_overview đã tạo trong init.sql).
     * Có thể lọc theo storeId, keyword tên hoặc SKU.
     */
    public List<InventoryOverview> getInventoryOverview(Long storeId, String keyword, int page, int pageSize) throws SQLException {
        if (page < 1) page = 1; if (pageSize <= 0) pageSize = 20; int offset = (page - 1) * pageSize;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT store_id, store_code, store_name, product_id, sku, product_name, quantity, updated_at ")
          .append("FROM v_inventory_overview WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (storeId != null) { sb.append("AND store_id = ? "); params.add(storeId); }
        if (keyword != null && !keyword.isBlank()) { sb.append("AND (product_name LIKE ? OR sku LIKE ?) "); String kw = "%" + keyword.trim() + "%"; params.add(kw); params.add(kw); }
        sb.append("ORDER BY store_name, product_name LIMIT ? OFFSET ?");
        params.add(pageSize); params.add(offset);

        List<InventoryOverview> list = new ArrayList<>();
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryOverview o = new InventoryOverview();
                    o.storeId = rs.getLong("store_id");
                    o.storeCode = rs.getString("store_code");
                    o.storeName = rs.getString("store_name");
                    o.productId = rs.getLong("product_id");
                    o.sku = rs.getString("sku");
                    o.productName = rs.getString("product_name");
                    o.quantity = rs.getInt("quantity");
                    Timestamp ts = rs.getTimestamp("updated_at");
                    o.updatedAt = ts != null ? ts.toLocalDateTime() : null;
                    list.add(o);
                }
            }
        }
        return list;
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        long sup = rs.getLong("supplier_id");
        p.setSupplierId(rs.wasNull() ? null : sup);
        p.setSku(rs.getString("sku"));
        p.setName(rs.getString("name"));
        p.setUnit(rs.getString("unit"));
        p.setUnitPrice(rs.getBigDecimal("unit_price"));
        p.setUnitCost(rs.getBigDecimal("unit_cost"));
        p.setActive(rs.getBoolean("active"));
        Timestamp ts = rs.getTimestamp("created_at");
        p.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return p;
    }

    // DTO nhỏ cho view tồn kho – để tránh tạo model riêng
    public static class InventoryOverview {
        public long storeId; public String storeCode; public String storeName;
        public long productId; public String sku; public String productName;
        public int quantity; public LocalDateTime updatedAt;
    }
}