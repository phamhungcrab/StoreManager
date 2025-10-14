package com.example.storemanagement.dao;

/*
 * FinanceDAO – CRUD báo cáo tài chính + lọc theo store/date/type và tổng hợp.
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.storemanagement.model.FinanceReport;

public class FinanceDAO {

    public long insert(FinanceReport r) throws SQLException {
        String sql = "INSERT INTO finance_reports(store_id, report_date, type, category, amount, note) VALUES(?,?,?,?,?,?)";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, r.getStoreId());
            ps.setDate(2, Date.valueOf(r.getReportDate()));
            ps.setString(3, r.getType().name());
            ps.setString(4, r.getCategory());
            ps.setBigDecimal(5, r.getAmount());
            ps.setString(6, r.getNote());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getLong(1); }
        }
        return -1L;
    }

    public boolean update(FinanceReport r) throws SQLException {
        String sql = "UPDATE finance_reports SET store_id=?, report_date=?, type=?, category=?, amount=?, note=? WHERE id=?";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, r.getStoreId());
            ps.setDate(2, Date.valueOf(r.getReportDate()));
            ps.setString(3, r.getType().name());
            ps.setString(4, r.getCategory());
            ps.setBigDecimal(5, r.getAmount());
            ps.setString(6, r.getNote());
            ps.setLong(7, r.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM finance_reports WHERE id=?";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Optional<FinanceReport> findById(long id) throws SQLException {
        String sql = "SELECT id, store_id, report_date, type, category, amount, note, created_at FROM finance_reports WHERE id=?";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(map(rs)); }
            return Optional.empty();
        }
    }

    /**
     * Lọc theo storeId (tuỳ chọn), khoảng ngày (tuỳ chọn), type (tuỳ chọn), có phân trang.
     */
    public List<FinanceReport> filter(Long storeId, LocalDate from, LocalDate to, FinanceReport.Type type, int page, int pageSize) throws SQLException {
        if (page < 1) page = 1; if (pageSize <= 0) pageSize = 20; int offset = (page - 1) * pageSize;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT id, store_id, report_date, type, category, amount, note, created_at FROM finance_reports WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (storeId != null) { sb.append("AND store_id = ? "); params.add(storeId); }
        if (from != null)    { sb.append("AND report_date >= ? "); params.add(Date.valueOf(from)); }
        if (to != null)      { sb.append("AND report_date <= ? "); params.add(Date.valueOf(to)); }
        if (type != null)    { sb.append("AND type = ? "); params.add(type.name()); }
        sb.append("ORDER BY report_date DESC, id DESC LIMIT ? OFFSET ?");
        params.add(pageSize); params.add(offset);

        List<FinanceReport> list = new ArrayList<>();
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /**
     * Tổng hợp: trả về income, expense, balance cho bộ lọc hiện tại.
     */
    public Summary summarize(Long storeId, LocalDate from, LocalDate to, FinanceReport.Type type) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
          .append("SUM(CASE WHEN type='INCOME' THEN amount ELSE 0 END) AS income, ")
          .append("SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END) AS expense ")
          .append("FROM finance_reports WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (storeId != null) { sb.append("AND store_id = ? "); params.add(storeId); }
        if (from != null)    { sb.append("AND report_date >= ? "); params.add(Date.valueOf(from)); }
        if (to != null)      { sb.append("AND report_date <= ? "); params.add(Date.valueOf(to)); }
        if (type != null)    { sb.append("AND type = ? "); params.add(type.name()); }

        BigDecimal income = BigDecimal.ZERO; BigDecimal expense = BigDecimal.ZERO;
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    income = rs.getBigDecimal("income"); if (income == null) income = BigDecimal.ZERO;
                    expense = rs.getBigDecimal("expense"); if (expense == null) expense = BigDecimal.ZERO;
                }
            }
        }
        return new Summary(income, expense, income.subtract(expense));
    }

    private FinanceReport map(ResultSet rs) throws SQLException {
        FinanceReport r = new FinanceReport();
        r.setId(rs.getLong("id"));
        r.setStoreId(rs.getLong("store_id"));
        Date d = rs.getDate("report_date");
        r.setReportDate(d != null ? d.toLocalDate() : null);
        String t = rs.getString("type");
        r.setType(t != null ? FinanceReport.Type.valueOf(t) : null);
        r.setCategory(rs.getString("category"));
        r.setAmount(rs.getBigDecimal("amount"));
        r.setNote(rs.getString("note"));
        Timestamp ts = rs.getTimestamp("created_at");
        r.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return r;
    }

    // DTO tổng hợp đơn giản
    public static class Summary {
        public final BigDecimal income; public final BigDecimal expense; public final BigDecimal balance;
        public Summary(BigDecimal income, BigDecimal expense, BigDecimal balance) {
            this.income = income; this.expense = expense; this.balance = balance;
        }
    }
}