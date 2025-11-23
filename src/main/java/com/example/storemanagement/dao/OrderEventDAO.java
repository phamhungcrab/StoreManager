package com.example.storemanagement.dao;

import com.example.storemanagement.model.OrderEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderEventDAO {

    public List<OrderEvent> getAllEvents() {
        List<OrderEvent> list = new ArrayList<>();
        // Query joins: events -> orders -> customers
        // And subquery/join for items aggregation
        String sql = "SELECT oe.*, " +
                "       c.name AS customer_name, " +
                "       o.total_amount, " +
                "       (SELECT GROUP_CONCAT(CONCAT(p.name, ' x', oi.quantity) SEPARATOR ', ') " +
                "        FROM order_items oi " +
                "        JOIN products p ON oi.product_id = p.id " +
                "        WHERE oi.order_id = o.id) AS product_details " +
                "FROM order_events oe " +
                "JOIN orders o ON oe.order_id = o.id " +
                "LEFT JOIN customers c ON o.customer_id = c.id " +
                "ORDER BY oe.created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OrderEvent e = new OrderEvent();
                e.setId(rs.getLong("id"));
                e.setOrderId(rs.getLong("order_id"));
                e.setEventType(rs.getString("event_type"));
                e.setDataJson(rs.getString("data_json"));
                e.setNote(rs.getString("note"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    e.setCreatedAt(ts.toLocalDateTime());
                }

                // Populate extended fields
                e.setCustomerName(rs.getString("customer_name"));
                if (e.getCustomerName() == null)
                    e.setCustomerName("Khách lẻ");

                e.setTotalAmount(rs.getBigDecimal("total_amount"));
                e.setProductDetails(rs.getString("product_details"));

                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Get order events filtered by store and date range
     * 
     * @param storeId  Store ID (null for all stores)
     * @param fromDate Start date (null for no start limit)
     * @param toDate   End date (null for no end limit)
     * @return List of order events
     */
    public List<OrderEvent> getEventsByStore(Long storeId, java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        List<OrderEvent> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT oe.*, ")
                .append("       c.name AS customer_name, ")
                .append("       o.total_amount, o.order_code, o.store_id, ")
                .append("       (SELECT GROUP_CONCAT(CONCAT(p.name, ' x', oi.quantity) SEPARATOR ', ') ")
                .append("        FROM order_items oi ")
                .append("        JOIN products p ON oi.product_id = p.id ")
                .append("        WHERE oi.order_id = o.id) AS product_details ")
                .append("FROM order_events oe ")
                .append("JOIN orders o ON oe.order_id = o.id ")
                .append("LEFT JOIN customers c ON o.customer_id = c.id ")
                .append("WHERE 1=1 ");

        if (storeId != null) {
            sql.append("AND o.store_id = ? ");
        }
        if (fromDate != null) {
            sql.append("AND DATE(oe.created_at) >= ? ");
        }
        if (toDate != null) {
            sql.append("AND DATE(oe.created_at) <= ? ");
        }
        sql.append("ORDER BY oe.created_at DESC");

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (storeId != null) {
                ps.setLong(paramIndex++, storeId);
            }
            if (fromDate != null) {
                ps.setDate(paramIndex++, java.sql.Date.valueOf(fromDate));
            }
            if (toDate != null) {
                ps.setDate(paramIndex++, java.sql.Date.valueOf(toDate));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderEvent e = new OrderEvent();
                    e.setId(rs.getLong("id"));
                    e.setOrderId(rs.getLong("order_id"));
                    e.setEventType(rs.getString("event_type"));
                    e.setDataJson(rs.getString("data_json"));
                    e.setNote(rs.getString("note"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        e.setCreatedAt(ts.toLocalDateTime());
                    }

                    e.setCustomerName(rs.getString("customer_name"));
                    if (e.getCustomerName() == null)
                        e.setCustomerName("Khách lẻ");

                    e.setTotalAmount(rs.getBigDecimal("total_amount"));
                    e.setProductDetails(rs.getString("product_details"));

                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
