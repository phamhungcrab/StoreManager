package com.example.storemanagement.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.example.storemanagement.dao.DBConnection;
import com.example.storemanagement.dao.ProductDAO;
import com.example.storemanagement.model.Product;

public class InventoryService {

    private final ProductDAO productDAO = new ProductDAO();

    // ===================== Sản phẩm =====================

    public long addProduct(Product p) throws SQLException {
        validateProduct(p, true);
        long id = productDAO.insert(p);

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO inventory(store_id, product_id, quantity) " +
                                "SELECT id, ?, 0 FROM stores")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }

        return id;
    }

    public boolean updateProduct(Product p) throws SQLException {
        if (p.getId() == null)
            throw new IllegalArgumentException("Thiếu ID sản phẩm");
        validateProduct(p, false);
        return productDAO.update(p);
    }

    public boolean deleteProduct(long id) throws SQLException {
        return productDAO.delete(id);
    }

    public Optional<Product> findProductById(long id) throws SQLException {
        return productDAO.findById(id);
    }

    public List<Product> searchProducts(String keyword, int page, int pageSize) throws SQLException {
        return productDAO.search(keyword, page, pageSize);
    }

    // ===================== Kho hàng =====================

    public void importStock(long storeId, long productId, int quantity, String note) throws SQLException {
        if (storeId <= 0 || productId <= 0)
            throw new IllegalArgumentException("storeId/productId không hợp lệ");
        if (quantity <= 0)
            throw new IllegalArgumentException("Số lượng phải > 0");
        productDAO.importStock(storeId, productId, quantity, note);
    }

    public void exportStock(long storeId, long productId, int quantity, String note) throws SQLException {
        if (storeId <= 0 || productId <= 0)
            throw new IllegalArgumentException("storeId/productId không hợp lệ");
        if (quantity <= 0)
            throw new IllegalArgumentException("Số lượng phải > 0");
        productDAO.exportStock(storeId, productId, quantity, note);
    }

    public void transferStock(long fromStoreId, long toStoreId, long productId, int quantity, String note)
            throws SQLException {
        if (fromStoreId == toStoreId) {
            throw new IllegalArgumentException("Kho xuất và kho nhập phải khác nhau");
        }
        // Transactional logic is handled by individual calls for now,
        // ideally should be wrapped in a single transaction block.
        // Since we don't have explicit transaction manager here, we rely on optimistic
        // flow.
        // 1. Export from source
        exportStock(fromStoreId, productId, quantity, "Transfer OUT to store #" + toStoreId + ": " + note);
        // 2. Import to dest
        importStock(toStoreId, productId, quantity, "Transfer IN from store #" + fromStoreId + ": " + note);
    }

    public List<ProductDAO.InventoryOverview> getInventoryOverview(Long storeId, Long supplierId, String keyword,
            int page, int pageSize) throws SQLException {
        return productDAO.getInventoryOverview(storeId, supplierId, keyword, page, pageSize);
    }

    // ===================== VALIDATION =====================

    private void validateProduct(Product p, boolean creating) {
        if (p == null)
            throw new IllegalArgumentException("Thiếu dữ liệu sản phẩm");
        if (creating && (p.getSku() == null || p.getSku().trim().isEmpty()))
            throw new IllegalArgumentException("SKU không được để trống");
        if (p.getName() == null || p.getName().trim().isEmpty())
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        if (p.getUnitPrice() != null && p.getUnitPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Giá bán không hợp lệ");
        if (p.getUnitCost() != null && p.getUnitCost().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Giá vốn không hợp lệ");
    }
}