package com.example.storemanagement.service;

/*
 * FinanceService – Tầng nghiệp vụ Tài chính
 * - Thêm/Sửa/Xoá báo cáo thu/chi
 * - Lọc & Phân trang theo store/date/type
 * - Tổng hợp Income/Expense/Balance
 */

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.storemanagement.dao.FinanceDAO;
import com.example.storemanagement.model.FinanceReport;

public class FinanceService {

    private final FinanceDAO financeDAO = new FinanceDAO();

    // ===================== CRUD =====================

    public long addIncome(long storeId, LocalDate date, BigDecimal amount, String category, String note) throws SQLException {
        return add(storeId, date, amount, category, note, FinanceReport.Type.INCOME);
    }

    public long addExpense(long storeId, LocalDate date, BigDecimal amount, String category, String note) throws SQLException {
        return add(storeId, date, amount, category, note, FinanceReport.Type.EXPENSE);
    }

    public boolean updateReport(FinanceReport r) throws SQLException {
        validateReport(r);
        return financeDAO.update(r);
    }

    public boolean deleteReport(long id) throws SQLException {
        return financeDAO.delete(id);
    }

    public Optional<FinanceReport> getById(long id) throws SQLException {
        return financeDAO.findById(id);
    }

    // ===================== Query & Summary =====================

    public List<FinanceReport> filter(Long storeId, LocalDate from, LocalDate to, FinanceReport.Type type,
                                      int page, int pageSize) throws SQLException {
        return financeDAO.filter(storeId, from, to, type, page, pageSize);
    }

    public FinanceDAO.Summary summarize(Long storeId, LocalDate from, LocalDate to, FinanceReport.Type type) throws SQLException {
        return financeDAO.summarize(storeId, from, to, type);
    }

    // ===================== Helpers =====================

    private long add(long storeId, LocalDate date, BigDecimal amount, String category, String note, FinanceReport.Type type) throws SQLException {
        if (storeId <= 0) throw new IllegalArgumentException("storeId không hợp lệ");
        if (date == null) throw new IllegalArgumentException("Ngày báo cáo không được để trống");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền phải > 0");
        FinanceReport r = new FinanceReport();
        r.setStoreId(storeId);
        r.setReportDate(date);
        r.setType(type);
        r.setCategory(category);
        r.setAmount(amount);
        r.setNote(note);
        return financeDAO.insert(r);
    }

    private void validateReport(FinanceReport r) {
        if (r == null) throw new IllegalArgumentException("Thiếu dữ liệu báo cáo");
        if (r.getId() == null) throw new IllegalArgumentException("Thiếu ID báo cáo");
        if (r.getStoreId() == null || r.getStoreId() <= 0) throw new IllegalArgumentException("storeId không hợp lệ");
        if (r.getReportDate() == null) throw new IllegalArgumentException("Ngày báo cáo không được để trống");
        if (r.getAmount() == null || r.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền phải > 0");
        if (r.getType() == null) throw new IllegalArgumentException("Type không hợp lệ");
    }
}