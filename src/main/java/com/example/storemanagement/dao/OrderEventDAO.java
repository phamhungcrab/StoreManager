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
}
