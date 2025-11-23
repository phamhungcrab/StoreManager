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
 * DAO for budget allocations from accountant to stores.
 */
public class BudgetAllocationDAO {

    /**
     * Allocate budget to a store. Trigger will automatically update
     * stores.cash_balance.
     */
    public long allocate(long storeId, BigDecimal amount, String note, String username) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        String sql = "INSERT INTO budget_allocations (store_id, amount, note, created_by) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, storeId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, note);
            ps.setString(4, username);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get generated ID");
        }
    }

    /**
     * Get allocation history for a store.
     */
    public List<BudgetAllocation> getHistory(long storeId, LocalDate fromDate, LocalDate toDate)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, store_id, amount, note, created_at, created_by " +
                        "FROM budget_allocations WHERE store_id = ?");

        if (fromDate != null) {
            sql.append(" AND DATE(created_at) >= ?");
        }
        if (toDate != null) {
            sql.append(" AND DATE(created_at) <= ?");
        }
        sql.append(" ORDER BY created_at DESC");

        List<BudgetAllocation> result = new ArrayList<>();

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
     * Get all allocations across all stores (for accountant overview).
     */
    public List<BudgetAllocation> getAllHistory(LocalDate fromDate, LocalDate toDate) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, store_id, amount, note, created_at, created_by " +
                        "FROM budget_allocations WHERE 1=1");

        if (fromDate != null) {
            sql.append(" AND DATE(created_at) >= ?");
        }
        if (toDate != null) {
            sql.append(" AND DATE(created_at) <= ?");
        }
        sql.append(" ORDER BY created_at DESC");

        List<BudgetAllocation> result = new ArrayList<>();

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            int idx = 1;
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

    private BudgetAllocation map(ResultSet rs) throws SQLException {
        return new BudgetAllocation(
                rs.getLong("id"),
                rs.getLong("store_id"),
                rs.getBigDecimal("amount"),
                rs.getString("note"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("created_by"));
    }

    // ===== Inner Class =====

    public static class BudgetAllocation {
        public final long id;
        public final long storeId;
        public final BigDecimal amount;
        public final String note;
        public final java.time.LocalDateTime createdAt;
        public final String createdBy;

        public BudgetAllocation(long id, long storeId, BigDecimal amount, String note,
                java.time.LocalDateTime createdAt, String createdBy) {
            this.id = id;
            this.storeId = storeId;
            this.amount = amount;
            this.note = note;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
        }
    }
}
