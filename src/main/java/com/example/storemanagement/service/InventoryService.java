package com.example.storemanagement.service;

/*
 * InventoryService – Tầng nghiệp vụ cho Kho hàng / Tài nguyên
 * - Quản lý sản phẩm (CRUD)
 * - Nhập/Xuất kho qua ProductDAO → stock_moves (trigger sẽ cập nhật inventory)
 * - Truy vấn tổng quan tồn kho (view v_inventory_overview)
 */

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.example.storemanagement.dao.ProductDAO;
import com.example.storemanagement.model.Product;

public class InventoryService {

    private final ProductDAO productDAO = new ProductDAO();

    // ===================== Sản phẩm =====================

    public long addProduct(Product p) throws SQLException {
        validateProduct(p, true);
        return productDAO.insert(p);
    }

    public boolean updateProduct(Product p) throws SQLException {
        if (p.getId() == null) throw new IllegalArgumentException("Thiếu ID sản phẩm");
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
        if (storeId <= 0 || productId <= 0) throw new IllegalArgumentException("storeId/productId không hợp lệ");
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải > 0");
        productDAO.importStock(storeId, productId, quantity, note);
    }

    public void exportStock(long storeId, long productId, int quantity, String note) throws SQLException {
        if (storeId <= 0 || productId <= 0) throw new IllegalArgumentException("storeId/productId không hợp lệ");
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải > 0");
        productDAO.exportStock(storeId, productId, quantity, note); // trigger sẽ chặn âm kho
    }

    public List<ProductDAO.InventoryOverview> getInventoryOverview(Long storeId, String keyword, int page, int pageSize) throws SQLException {
        return productDAO.getInventoryOverview(storeId, keyword, page, pageSize);
    }

    // ===================== VALIDATION =====================

    private void validateProduct(Product p, boolean creating) {
        if (p == null) throw new IllegalArgumentException("Thiếu dữ liệu sản phẩm");
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