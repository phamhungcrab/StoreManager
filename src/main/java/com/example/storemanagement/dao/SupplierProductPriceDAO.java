package com.example.storemanagement.dao;

import com.example.storemanagement.model.SupplierProductPrice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierProductPriceDAO {

    public SupplierProductPriceDAO() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS supplier_product_prices (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "product_id BIGINT NOT NULL, " +
                "supplier_id BIGINT NOT NULL, " +
                "import_price DECIMAL(15,2) NOT NULL, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (product_id) REFERENCES products(id), " +
                "FOREIGN KEY (supplier_id) REFERENCES suppliers(id), " +
                "UNIQUE KEY unique_price (product_id, supplier_id)" +
                ")";
        try (Connection cn = DBConnection.getInstance().getConnection();
                Statement stmt = cn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void upsert(SupplierProductPrice p) throws SQLException {
        String sql = "INSERT INTO supplier_product_prices (product_id, supplier_id, import_price) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE import_price = VALUES(import_price)";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, p.getProductId());
            ps.setLong(2, p.getSupplierId());
            ps.setBigDecimal(3, p.getImportPrice());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM supplier_product_prices WHERE id = ?";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public List<SupplierProductPrice> getAll() throws SQLException {
        List<SupplierProductPrice> list = new ArrayList<>();
        String sql = "SELECT spp.*, p.name as product_name, p.sku, s.name as supplier_name " +
                "FROM supplier_product_prices spp " +
                "JOIN products p ON spp.product_id = p.id " +
                "JOIN suppliers s ON spp.supplier_id = s.id " +
                "ORDER BY p.name, s.name";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SupplierProductPrice p = new SupplierProductPrice();
                p.setId(rs.getLong("id"));
                p.setProductId(rs.getLong("product_id"));
                p.setSupplierId(rs.getLong("supplier_id"));
                p.setImportPrice(rs.getBigDecimal("import_price"));
                Timestamp ts = rs.getTimestamp("updated_at");
                p.setUpdatedAt(ts != null ? ts.toLocalDateTime() : null);
                p.setProductName(rs.getString("product_name"));
                p.setSku(rs.getString("sku"));
                p.setSupplierName(rs.getString("supplier_name"));
                list.add(p);
            }
        }
        return list;
    }

    public List<SupplierProductPrice> search(String keyword) throws SQLException {
        List<SupplierProductPrice> list = new ArrayList<>();
        String sql = "SELECT spp.*, p.name as product_name, p.sku, s.name as supplier_name " +
                "FROM supplier_product_prices spp " +
                "JOIN products p ON spp.product_id = p.id " +
                "JOIN suppliers s ON spp.supplier_id = s.id " +
                "WHERE p.name LIKE ? OR p.sku LIKE ? OR s.name LIKE ? " +
                "ORDER BY p.name, s.name";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SupplierProductPrice p = new SupplierProductPrice();
                    p.setId(rs.getLong("id"));
                    p.setProductId(rs.getLong("product_id"));
                    p.setSupplierId(rs.getLong("supplier_id"));
                    p.setImportPrice(rs.getBigDecimal("import_price"));
                    Timestamp ts = rs.getTimestamp("updated_at");
                    p.setUpdatedAt(ts != null ? ts.toLocalDateTime() : null);
                    p.setProductName(rs.getString("product_name"));
                    p.setSku(rs.getString("sku"));
                    p.setSupplierName(rs.getString("supplier_name"));
                    list.add(p);
                }
            }
        }
        return list;
    }
}
