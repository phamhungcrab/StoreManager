package com.example.storemanagement.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.example.storemanagement.dao.DBConnection;
import com.example.storemanagement.dao.OrderDAO;
import com.example.storemanagement.util.PointPolicy;

/**
 * OrderService – chốt đơn (transactional):
 *  1) Tạo order (CONFIRMED)
 *  2) Thêm items (trigger sẽ trừ kho + cập nhật subtotal)
 *  3) Ghi finance_reports (INCOME: Sales)
 *  4) Cộng điểm customers + ghi points_ledger
 *  5) Ghi order_events (CREATED/CONFIRMED/PAID tuỳ bạn)
 */
public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();

    // DTO input tối giản cho 1 dòng hàng
    public static class OrderItemInput {
        public long productId; public int quantity; public OrderItemInput(long p, int q){this.productId=p;this.quantity=q;}
    }

    /**
     * Finalize order và trả về orderId.
     */
    public long finalizeOrder(long storeId, Long customerId, List<OrderItemInput> items,
                              BigDecimal discount, String note, String paymentMethod) throws SQLException {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Danh sách hàng trống");
        if (discount == null) discount = BigDecimal.ZERO;
        if (discount.signum() < 0) throw new IllegalArgumentException("Discount không hợp lệ");

        Connection cn = DBConnection.getInstance().getConnection();
        boolean oldAuto = cn.getAutoCommit();
        cn.setAutoCommit(false); // bắt đầu TRANSACTION
        try {
            // 1) Insert ORDER (subtotal tạm 0, discount lưu riêng)
            com.example.storemanagement.model.Order o = new com.example.storemanagement.model.Order();
            o.setOrderCode(genOrderCode());
            o.setStoreId(storeId);
            o.setCustomerId(customerId);
            o.setTotalAmount(BigDecimal.ZERO); // sẽ được trigger cập nhật sau khi có items
            o.setDiscount(discount);
            o.setStatus(com.example.storemanagement.model.Order.Status.CONFIRMED);
            o.setNote(note);
            long orderId = orderDAO.insertOrder(cn, o);

            // 2) Insert ITEMS (đọc unit_price từ products tại thời điểm chốt)
            for (OrderItemInput in : items) {
                BigDecimal unitPrice = readProductPrice(cn, in.productId);
                insertOrderItem(cn, orderId, in.productId, in.quantity, unitPrice);
            }

            // Lấy SUBTOTAL do trigger đã tính xong
            BigDecimal subtotal = orderDAO.getSubtotal(cn, orderId);
            if (subtotal == null) subtotal = BigDecimal.ZERO;
            BigDecimal grandTotal = subtotal.subtract(discount);
            if (grandTotal.signum() < 0) grandTotal = BigDecimal.ZERO;

            // 3) Ghi FINANCE (INCOME: Sales)
            insertFinanceIncome(cn, storeId, grandTotal, "Sales", "Order #" + o.getOrderCode());

            // 4) Cộng điểm (nếu có khách hàng)
            if (customerId != null) {
                int points = PointPolicy.calcPoints(grandTotal);
                if (points > 0) {
                    addCustomerPoints(cn, customerId, points);
                    insertPointsLedger(cn, customerId, orderId, points, "PURCHASE", "Order #" + o.getOrderCode());
                }
            }

            // 5) Log events
            insertOrderEvent(cn, orderId, "CREATED", null, note);
            insertOrderEvent(cn, orderId, "CONFIRMED", null, paymentMethod);

            cn.commit();
            return orderId;
        } catch (Exception ex) {
            cn.rollback();
            if (ex instanceof SQLException) throw (SQLException) ex;
            throw new SQLException("Finalize order thất bại: " + ex.getMessage(), ex);
        } finally {
            cn.setAutoCommit(oldAuto);
        }
    }

    public List<com.example.storemanagement.model.Order> listOrdersByCustomer(long customerId, int page, int pageSize) throws SQLException {
        try (Connection cn = DBConnection.getInstance().getConnection()) {
            return orderDAO.listByCustomer(cn, customerId, page, pageSize);
        }
    }

    // ==================== Low-level helpers (trong chung 1 transaction) ====================
    private String genOrderCode() { // ví dụ: ORD-2025-xxxxx
        String ts = java.time.LocalDate.now().toString().replace("-", "");
        String rnd = String.format("%05d", new java.util.Random().nextInt(100000));
        return "ORD-" + ts + "-" + rnd;
    }

    private BigDecimal readProductPrice(Connection cn, long productId) throws SQLException {
        String sql = "SELECT unit_price FROM products WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getBigDecimal(1); }
        }
        throw new SQLException("Không tìm thấy sản phẩm #" + productId);
    }

    private void insertOrderItem(Connection cn, long orderId, long productId, int qty, BigDecimal unitPrice) throws SQLException {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng phải > 0");
        String sql = "INSERT INTO order_items(order_id, product_id, quantity, unit_price) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, productId);
            ps.setInt(3, qty);
            ps.setBigDecimal(4, unitPrice);
            ps.executeUpdate();
        }
    }

    private void insertFinanceIncome(Connection cn, long storeId, BigDecimal amount, String category, String note) throws SQLException {
        String sql = "INSERT INTO finance_reports(store_id, report_date, type, category, amount, note)" +
                     " VALUES (?, CURRENT_DATE(), 'INCOME', ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, storeId);
            ps.setString(2, category);
            ps.setBigDecimal(3, amount);
            ps.setString(4, note);
            ps.executeUpdate();
        }
    }

    private void addCustomerPoints(Connection cn, long customerId, int delta) throws SQLException {
        String sql = "UPDATE customers SET points = GREATEST(0, points + ?), created_at = created_at WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setLong(2, customerId);
            ps.executeUpdate();
        }
    }

    private void insertPointsLedger(Connection cn, long customerId, long orderId, int delta, String reason, String note) throws SQLException {
        String sql = "INSERT INTO points_ledger(customer_id, order_id, delta, reason, note) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setLong(2, orderId);
            ps.setInt(3, delta);
            ps.setString(4, reason);
            ps.setString(5, note);
            ps.executeUpdate();
        }
    }

    private void insertOrderEvent(Connection cn, long orderId, String eventType, String dataJson, String note) throws SQLException {
        String sql = "INSERT INTO order_events(order_id, event_type, data_json, note) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setString(2, eventType);
            ps.setString(3, dataJson);
            ps.setString(4, note);
            ps.executeUpdate();
        }
    }
}