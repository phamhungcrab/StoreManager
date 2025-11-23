package com.example.storemanagement.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for budget expenses recorded by sellers.
 */
public class BudgetExpenseDAO {

    /**
     * Record an expense. Trigger will automatically update stores.cash_balance.
     * Will throw SQLException if insufficient balance.
     */
    public long recordExpense(long storeId, BigDecimal amount, String reason, String note, String username)
            throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required");
        }

        String sql = "INSERT INTO budget_expenses (store_id, amount, reason, note, created_by) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, storeId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, reason);
            ps.setString(4, note);
            ps.setString(5, username);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get generated ID");
        }
    }

    /**
     * Get expense history for a store.
     */
    public List<BudgetExpense> getHistory(long storeId, LocalDate fromDate, LocalDate toDate)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, store_id, amount, reason, note, created_at, created_by " +
                        "FROM budget_expenses WHERE store_id = ?");

        if (fromDate != null) {
            sql.append(" AND DATE(created_at) >= ?");
        }
        if (toDate != null) {
            sql.append(" AND DATE(created_at) <= ?");
        }
        sql.append(" ORDER BY created_at DESC");

        List<BudgetExpense> result = new ArrayList<>();

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setLong(idx++, storeId);
            if (fromDate != null)
                ps.setObject(idx++, fromDate);
            if (toDate != null)
                ps.setObject(idx++, toDate);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(map(rs));
            }
        }
        return result;
    }

    /**
     * Get current cash balance for a store.
     */
    public BigDecimal getCurrentBalance(long storeId) throws SQLException {
        String sql = "SELECT cash_balance FROM stores WHERE id = ?";

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, storeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("cash_balance");
            }
            throw new SQLException("Store not found: " + storeId);
        }
    }

    private BudgetExpense map(ResultSet rs) throws SQLException {
        return new BudgetExpense(
                rs.getLong("id"),
                rs.getLong("store_id"),
                rs.getBigDecimal("amount"),
                rs.getString("reason"),
                rs.getString("note"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("created_by"));
    }

    // ===== Inner Class =====

    public static class BudgetExpense {
        public final long id;
        public final long storeId;
        public final BigDecimal amount;
        public final String reason;
        public final String note;
        public final java.time.LocalDateTime createdAt;
        public final String createdBy;

        public BudgetExpense(long id, long storeId, BigDecimal amount, String reason, String note,
                java.time.LocalDateTime createdAt, String createdBy) {
            this.id = id;
            this.storeId = storeId;
            this.amount = amount;
            this.reason = reason;
            this.note = note;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
        }
    }
}
