package com.example.storemanagement.dao; // Package DAO: nơi chứa lớp truy cập dữ liệu (Data Access Object)

/*
 * ProductDAO – CRUD sản phẩm + tra cứu tổng quan tồn kho (view v_inventory_overview)
 * và thao tác nhập/xuất kho qua bảng stock_moves (trigger sẽ cập nhật inventory).
 *
 * (Phiên bản này CHỈ bổ sung chú thích giải thích từng dòng; KHÔNG thay đổi logic/code.)
 */

import java.math.BigDecimal;            // Dùng cho tiền tệ/giá (tránh lỗi làm tròn so với double)
import java.sql.Connection;             // Đại diện kết nối tới DB (JDBC)
import java.sql.PreparedStatement;      // Câu lệnh SQL có tham số (?) an toàn, chống SQL injection
import java.sql.ResultSet;              // Kết quả trả về của câu lệnh SELECT
import java.sql.SQLException;           // Ngoại lệ JDBC
import java.sql.Statement;              // Dùng để yêu cầu trả về generated keys
import java.sql.Timestamp;              // Kiểu thời gian JDBC (map sang LocalDateTime)
import java.sql.Types;                  // Hằng số JDBC cho kiểu dữ liệu (dùng setNull)
import java.time.LocalDateTime;         // Thời gian hiện đại của Java
import java.util.ArrayList;             // Danh sách động
import java.util.List;                  // Interface list
import java.util.Optional;              // Gói kết quả có/không có

import com.example.storemanagement.model.Product; // Model sản phẩm (POJO)

public class ProductDAO {               // Lớp DAO làm việc với bảng products và view tồn kho

    // ===================== CRUD sản phẩm =====================
    public long insert(Product p) throws SQLException {  // Thêm sản phẩm, trả về ID tự tăng
        String sql = "INSERT INTO products(supplier_id, sku, name, unit, unit_price, unit_cost, active) " +
                     "VALUES(?,?,?,?,?,?,?)";            // Câu SQL với 7 tham số
        try (Connection cn = DBConnection.getInstance().getConnection();               // Mở kết nối (tự đóng nhờ try-with-resources)
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Yêu cầu trả về khóa tự tăng
            if (p.getSupplierId() == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, p.getSupplierId()); // supplier_id có thể null
            ps.setString(2, p.getSku());                   // sku
            ps.setString(3, p.getName());                  // name
            ps.setString(4, p.getUnit());                  // đơn vị tính (pcs, box, ...)
            ps.setBigDecimal(5, p.getUnitPrice() != null ? p.getUnitPrice() : BigDecimal.ZERO); // giá bán
            ps.setBigDecimal(6, p.getUnitCost()  != null ? p.getUnitCost()  : BigDecimal.ZERO); // giá vốn
            ps.setBoolean(7, p.isActive());                // trạng thái hoạt động
            ps.executeUpdate();                            // Thực thi INSERT
            try (ResultSet rs = ps.getGeneratedKeys()) {   // Lấy khóa tự tăng (id)
                if (rs.next()) return rs.getLong(1);       // Trả về ID nếu có
            }
        }
        return -1L;                                        // Nếu không lấy được id, trả về -1
    }

    public boolean update(Product p) throws SQLException { // Cập nhật sản phẩm theo id
        String sql = "UPDATE products SET supplier_id=?, sku=?, name=?, unit=?, unit_price=?, unit_cost=?, active=? WHERE id=?"; // Câu UPDATE
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {         // Chuẩn bị câu lệnh
            if (p.getSupplierId() == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, p.getSupplierId()); // supplier_id null hay không
            ps.setString(2, p.getSku());              // sku
            ps.setString(3, p.getName());             // name
            ps.setString(4, p.getUnit());             // unit
            ps.setBigDecimal(5, p.getUnitPrice() != null ? p.getUnitPrice() : BigDecimal.ZERO); // unit_price
            ps.setBigDecimal(6, p.getUnitCost()  != null ? p.getUnitCost()  : BigDecimal.ZERO); // unit_cost
            ps.setBoolean(7, p.isActive());           // active
            ps.setLong(8, p.getId());                 // where id = ?
            return ps.executeUpdate() > 0;            // true nếu có ít nhất 1 dòng bị ảnh hưởng
        }
    }

    public boolean delete(long id) throws SQLException {  // Xóa sản phẩm theo id
        String sql = "DELETE FROM products WHERE id=?"; // Câu DELETE
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);                            // id cần xóa
            return ps.executeUpdate() > 0;                // Trả về true nếu xóa thành công
        }
    }

    public Optional<Product> findById(long id) throws SQLException { // Tìm sản phẩm theo id
        String sql = "SELECT id, supplier_id, sku, name, unit, unit_price, unit_cost, active, created_at FROM products WHERE id=?"; // Lấy đủ trường cần map
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);                            // Gán tham số id
            try (ResultSet rs = ps.executeQuery()) {      // Thực thi SELECT
                if (rs.next()) return Optional.of(map(rs)); // Nếu có bản ghi → map sang Product
            }
            return Optional.empty();                      // Không có → Optional rỗng
        }
    }

    public List<Product> search(String keyword, int page, int pageSize) throws SQLException { // Tìm kiếm có phân trang
        if (page < 1) page = 1; if (pageSize <= 0) pageSize = 20; // Bảo vệ tham số
        int offset = (page - 1) * pageSize;             // Tính vị trí bắt đầu
        String base = "SELECT id, supplier_id, sku, name, unit, unit_price, unit_cost, active, created_at FROM products "; // Phần đầu câu lệnh
        String where = "";                             // Phần WHERE tùy chọn
        boolean hasKw = keyword != null && !keyword.isBlank(); // Kiểm tra có từ khóa không
        if (hasKw) where = "WHERE (name LIKE ? OR sku LIKE ?) "; // Lọc theo name hoặc sku
        String tail = "ORDER BY created_at DESC LIMIT ? OFFSET ?"; // Sắp xếp mới nhất, phân trang

        List<Product> list = new ArrayList<>();         // Kết quả trả về
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(base + where + tail)) { // Ghép câu SQL đầy đủ
            int i = 1;                                   // Chỉ số tham số trong PreparedStatement (bắt đầu từ 1)
            if (hasKw) {                                 // Nếu có keyword → set 2 tham số LIKE
                String kw = "%" + keyword.trim() + "%"; // Thêm wildcard %
                ps.setString(i++, kw); ps.setString(i++, kw);
            }
            ps.setInt(i++, pageSize);                    // LIMIT
            ps.setInt(i, offset);                        // OFFSET
            try (ResultSet rs = ps.executeQuery()) {     // Thực thi
                while (rs.next()) list.add(map(rs));     // Lặp qua từng dòng kết quả và map sang Product
            }
        }
        return list;                                     // Trả về danh sách sản phẩm
    }

    // ===================== Tồn kho & Stock move =====================

    /**
     * Nhập kho: ghi vào stock_moves (trigger sẽ cộng tồn kho tương ứng)
     */
    public void importStock(long storeId, long productId, int quantity, String note) throws SQLException { // Ghi một dòng IMPORT vào stock_moves
        String sql = "INSERT INTO stock_moves(store_id, product_id, move_type, quantity, note) VALUES (?,?,?,?,?)"; // move_type = IMPORT
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, storeId);                     // cửa hàng
            ps.setLong(2, productId);                   // sản phẩm
            ps.setString(3, "IMPORT");                // kiểu di chuyển = IMPORT
            ps.setInt(4, quantity);                     // số lượng nhập
            ps.setString(5, note);                      // ghi chú
            ps.executeUpdate();                         // Triggers DB sẽ cập nhật bảng tồn kho
        }
    }

    /**
     * Xuất kho: ghi vào stock_moves (trigger sẽ trừ tồn kho; nếu âm → trigger chặn)
     */
    public void exportStock(long storeId, long productId, int quantity, String note) throws SQLException { // Ghi một dòng EXPORT vào stock_moves
        String sql = "INSERT INTO stock_moves(store_id, product_id, move_type, quantity, note) VALUES (?,?,?,?,?)"; // move_type = EXPORT
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, storeId);                     // cửa hàng
            ps.setLong(2, productId);                   // sản phẩm
            ps.setString(3, "EXPORT");                // kiểu di chuyển = EXPORT
            ps.setInt(4, quantity);                     // số lượng xuất
            ps.setString(5, note);                      // ghi chú
            ps.executeUpdate();                         // Triggers DB sẽ trừ tồn kho; nếu âm → báo lỗi phía DB
        }
    }

    /**
     * Tổng quan tồn kho (dùng view v_inventory_overview đã tạo trong init.sql).
     * Có thể lọc theo storeId, keyword tên hoặc SKU.
     */
    public List<InventoryOverview> getInventoryOverview(Long storeId, String keyword, int page, int pageSize) throws SQLException { // Lấy danh sách tồn kho tổng hợp
        if (page < 1) page = 1; if (pageSize <= 0) pageSize = 20; int offset = (page - 1) * pageSize; // Chuẩn hóa tham số
        StringBuilder sb = new StringBuilder();         // Dùng StringBuilder để ghép SQL theo điều kiện
        sb.append("SELECT store_id, store_code, store_name, product_id, sku, product_name, quantity, updated_at ")
          .append("FROM v_inventory_overview WHERE 1=1 "); // WHERE 1=1 để dễ nối AND tiếp theo
        List<Object> params = new ArrayList<>();        // Danh sách tham số cho PreparedStatement
        if (storeId != null) { sb.append("AND store_id = ? "); params.add(storeId); }   // Lọc theo cửa hàng nếu có
        if (keyword != null && !keyword.isBlank()) {    // Lọc theo tên hoặc SKU nếu có từ khóa
            sb.append("AND (product_name LIKE ? OR sku LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";  // wildcard cho LIKE
            params.add(kw); params.add(kw);
        }
        sb.append("ORDER BY store_name, product_name LIMIT ? OFFSET ?"); // Sắp xếp rồi phân trang
        params.add(pageSize); params.add(offset);       // Thêm LIMIT và OFFSET vào danh sách tham số

        List<InventoryOverview> list = new ArrayList<>(); // Kết quả trả về
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sb.toString())) {  // Tạo PreparedStatement với SQL đã ghép
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i)); // Gán lần lượt các tham số
            try (ResultSet rs = ps.executeQuery()) { // Thực thi SELECT
                while (rs.next()) {                 // Duyệt từng dòng kết quả
                    InventoryOverview o = new InventoryOverview(); // Tạo DTO tạm
                    o.storeId = rs.getLong("store_id");          // Map cột → trường
                    o.storeCode = rs.getString("store_code");
                    o.storeName = rs.getString("store_name");
                    o.productId = rs.getLong("product_id");
                    o.sku = rs.getString("sku");
                    o.productName = rs.getString("product_name");
                    o.quantity = rs.getInt("quantity");
                    Timestamp ts = rs.getTimestamp("updated_at"); // Lấy thời điểm cập nhật
                    o.updatedAt = ts != null ? ts.toLocalDateTime() : null; // Chuyển sang LocalDateTime an toàn null
                    list.add(o);                                   // Thêm vào danh sách kết quả
                }
            }
        }
        return list;                                                // Trả về danh sách tổng quan tồn kho
    }

    private Product map(ResultSet rs) throws SQLException { // Hàm tiện ích: map 1 dòng ResultSet → Product
        Product p = new Product();                         // Tạo đối tượng rỗng
        p.setId(rs.getLong("id"));                        // id
        long sup = rs.getLong("supplier_id");             // lấy supplier_id dạng long
        p.setSupplierId(rs.wasNull() ? null : sup);        // nếu cột là NULL → set null, ngược lại set giá trị
        p.setSku(rs.getString("sku"));                    // sku
        p.setName(rs.getString("name"));                  // name
        p.setUnit(rs.getString("unit"));                  // unit
        p.setUnitPrice(rs.getBigDecimal("unit_price"));   // unit_price
        p.setUnitCost(rs.getBigDecimal("unit_cost"));     // unit_cost
        p.setActive(rs.getBoolean("active"));             // active
        Timestamp ts = rs.getTimestamp("created_at");     // created_at
        p.setCreatedAt(ts != null ? ts.toLocalDateTime() : null); // map thời gian → LocalDateTime (cho phép null)
        return p;                                          // Trả về Product đã map xong
    }

    // DTO nhỏ cho view tồn kho – để tránh tạo model riêng
    public static class InventoryOverview {                // Lớp dữ liệu tạm thời phản ánh hàng trong view v_inventory_overview
        public long storeId; public String storeCode; public String storeName; // Thông tin cửa hàng
        public long productId; public String sku; public String productName;   // Thông tin sản phẩm
        public int quantity; public LocalDateTime updatedAt;                   // Số lượng tồn & thời điểm cập nhật
    }
}
