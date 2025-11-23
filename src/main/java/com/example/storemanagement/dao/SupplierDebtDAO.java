package com.example.storemanagement.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for managing supplier debts (accounts payable).
 * Accountant role uses this to track money owed to suppliers.
 */
public class SupplierDebtDAO {

    /**
     * Record a new debt to a supplier.
     */
    public long addDebt(long supplierId, BigDecimal amount, String note, String username) throws SQLException {
        String sql = "INSERT INTO supplier_debts (supplier_id, amount, transaction_type, note, created_by) " +
                "VALUES (?, ?, 'ADD_DEBT', ?, ?)";

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, supplierId);
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
     * Record a payment to reduce debt.
     */
    public long payDebt(long supplierId, BigDecimal amount, String note, String username) throws SQLException {
        String sql = "INSERT INTO supplier_debts (supplier_id, amount, transaction_type, note, created_by) " +
                "VALUES (?, ?, 'PAY_DEBT', ?, ?)";

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, supplierId);
            ps.setBigDecimal(2, amount.negate()); // Negative amount for payment
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
     * Get current balance for a specific supplier.
     * Positive = we owe them, Negative = they owe us.
     */
    public BigDecimal getSupplierBalance(long supplierId) throws SQLException {
        String sql = "SELECT IFNULL(SUM(amount), 0) AS balance " +
                "FROM supplier_debts WHERE supplier_id = ?";

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, supplierId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("balance");
            }
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get balances for all suppliers.
     * Returns Map<supplierId, SupplierDebtSummary>
     */
    public Map<Long, SupplierDebtSummary> getAllSupplierBalances() throws SQLException {
        String sql = "SELECT s.id, s.name, IFNULL(SUM(sd.amount), 0) AS balance " +
                "FROM suppliers s " +
                "LEFT JOIN supplier_debts sd ON s.id = sd.supplier_id " +
                "GROUP BY s.id, s.name " +
                "ORDER BY s.name";

        Map<Long, SupplierDebtSummary> result = new HashMap<>();

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SupplierDebtSummary summary = new SupplierDebtSummary(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("balance"));
                result.put(summary.id, summary);
            }
        }
        return result;
    }

    /**
     * Get transaction history for a supplier.
     */
    public List<SupplierDebtTransaction> getTransactionHistory(long supplierId, LocalDate fromDate, LocalDate toDate)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, supplier_id, amount, transaction_type, note, created_at, created_by " +
                        "FROM supplier_debts WHERE supplier_id = ?");

        if (fromDate != null) {
            sql.append(" AND DATE(created_at) >= ?");
        }
        if (toDate != null) {
            sql.append(" AND DATE(created_at) <= ?");
        }
        sql.append(" ORDER BY created_at DESC");

        List<SupplierDebtTransaction> result = new ArrayList<>();

        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setLong(idx++, supplierId);
            if (fromDate != null)
                ps.setObject(idx++, fromDate);
            if (toDate != null)
                ps.setObject(idx++, toDate);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapTransaction(rs));
            }
        }
        return result;
    }

    private SupplierDebtTransaction mapTransaction(ResultSet rs) throws SQLException {
        return new SupplierDebtTransaction(
                rs.getLong("id"),
                rs.getLong("supplier_id"),
                rs.getBigDecimal("amount"),
                rs.getString("transaction_type"),
                rs.getString("note"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("created_by"));
    }

    // ===== Inner Classes =====

    public static class SupplierDebtSummary {
        public final long id;
        public final String name;
        public final BigDecimal balance;

        public SupplierDebtSummary(long id, String name, BigDecimal balance) {
            this.id = id;
            this.name = name;
            this.balance = balance;
        }
    }

    public static class SupplierDebtTransaction {
        public final long id;
        public final long supplierId;
        public final BigDecimal amount;
        public final String transactionType;
        public final String note;
        public final java.time.LocalDateTime createdAt;
        public final String createdBy;

        public SupplierDebtTransaction(long id, long supplierId, BigDecimal amount,
                String transactionType, String note,
                java.time.LocalDateTime createdAt, String createdBy) {
            this.id = id;
            this.supplierId = supplierId;
            this.amount = amount;
            this.transactionType = transactionType;
            this.note = note;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
        }
    }
}
