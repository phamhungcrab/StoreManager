package com.example.storemanagement.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.storemanagement.model.Order;
import com.example.storemanagement.model.OrderItem;

/** DAO đặc thù cho đơn hàng. Chấp nhận truyền Connection để tham gia cùng transaction. */
public class OrderDAO {

    // =============== ORDER =================
    public long insertOrder(Connection cn, Order o) throws SQLException {
        String sql = "INSERT INTO orders(order_code, store_id, customer_id, total_amount, discount, status, note)" +
                     " VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, o.getOrderCode());
            ps.setLong(2, o.getStoreId());
            if (o.getCustomerId() == null) ps.setNull(3, Types.BIGINT); else ps.setLong(3, o.getCustomerId());
            ps.setBigDecimal(4, o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount());
            ps.setBigDecimal(5, o.getDiscount() == null ? BigDecimal.ZERO : o.getDiscount());
            ps.setString(6, o.getStatus() == null ? Order.Status.CONFIRMED.name() : o.getStatus().name());
            ps.setString(7, o.getNote());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getLong(1); }
        }
        throw new SQLException("Không lấy được ID order vừa tạo");
    }

    public Optional<Order> findById(Connection cn, long id) throws SQLException {
        String sql = "SELECT id, order_code, store_id, customer_id, total_amount, discount, status, note, created_at, paid_at" +
                     " FROM orders WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order o = mapOrder(rs);
                    return Optional.of(o);
                }
            }
        }
        return Optional.empty();
    }

    public List<Order> listByCustomer(Connection cn, long customerId, int page, int pageSize) throws SQLException {
        String sql = "SELECT id, order_code, store_id, customer_id, total_amount, discount, status, note, created_at, paid_at" +
                     " FROM orders WHERE customer_id=? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Order> list = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setInt(2, pageSize);
            ps.setInt(3, (page-1)*pageSize);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapOrder(rs)); }
        }
        return list;
    }

    public BigDecimal getSubtotal(Connection cn, long orderId) throws SQLException {
        String sql = "SELECT IFNULL(SUM(line_total),0) FROM order_items WHERE order_id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getBigDecimal(1); }
        }
        return BigDecimal.ZERO;
    }

    // =============== ITEMS =================
    public void insertItem(Connection cn, OrderItem it) throws SQLException {
        String sql = "INSERT INTO order_items(order_id, product_id, quantity, unit_price) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, it.getOrderId());
            ps.setLong(2, it.getProductId());
            ps.setInt(3, it.getQuantity());
            ps.setBigDecimal(4, it.getUnitPrice());
            ps.executeUpdate();
        }
    }

    public List<OrderItem> listItems(Connection cn, long orderId) throws SQLException {
        String sql = "SELECT id, order_id, product_id, quantity, unit_price, line_total FROM order_items WHERE order_id=?";
        List<OrderItem> list = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem it = new OrderItem();
                    it.setId(rs.getLong("id"));
                    it.setOrderId(rs.getLong("order_id"));
                    it.setProductId(rs.getLong("product_id"));
                    it.setQuantity(rs.getInt("quantity"));
                    it.setUnitPrice(rs.getBigDecimal("unit_price"));
                    it.setLineTotal(rs.getBigDecimal("line_total"));
                    list.add(it);
                }
            }
        }
        return list;
    }

    // =============== Helpers =================
    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setOrderCode(rs.getString("order_code"));
        o.setStoreId(rs.getLong("store_id"));
        long cid = rs.getLong("customer_id"); o.setCustomerId(rs.wasNull()? null: cid);
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setDiscount(rs.getBigDecimal("discount"));
        String st = rs.getString("status");
        o.setStatus(st == null ? null : Order.Status.valueOf(st));
        o.setNote(rs.getString("note"));
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) o.setCreatedAt(cAt.toLocalDateTime());
        Timestamp pAt = rs.getTimestamp("paid_at");
        if (pAt != null) o.setPaidAt(pAt.toLocalDateTime());
        return o;
    }
}