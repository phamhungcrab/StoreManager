package com.example.storemanagement.service;

/*
 * CustomerService – Tầng nghiệp vụ cho Khách hàng
 * - Validate dữ liệu nhập (name/email/phone)
 * - Gọi CustomerDAO để CRUD + search + điều chỉnh điểm
 * - Ẩn bớt chi tiết SQLException, trả message dễ hiểu
 */

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.example.storemanagement.dao.CustomerDAO;
import com.example.storemanagement.model.Customer;

public class CustomerService {

    private final CustomerDAO customerDAO = new CustomerDAO();

    // Regex đơn giản – có thể thay bằng validator xịn hơn sau này
    private static final Pattern EMAIL_RE = Pattern.compile("^[^@\n]+@[^@\n]+\\.[^@\n]+$");
    private static final Pattern PHONE_RE = Pattern.compile("^[0-9 +()-]{7,20}$");

    /** Tạo khách hàng mới sau khi validate. Trả về ID vừa tạo. */
    public long createCustomer(String name, String phone, String email) throws SQLException {
        validateName(name);
        validatePhone(phone);
        validateEmail(email);

        Customer c = new Customer();
        c.setName(name.trim());
        c.setPhone(isBlank(phone) ? null : phone.trim());
        c.setEmail(isBlank(email) ? null : email.trim());
        c.setPoints(0);
        c.setCreatedAt(LocalDateTime.now());

        try {
            return customerDAO.insert(c);
        } catch (SQLIntegrityConstraintViolationException dup) {
            // Bắt trùng email/phone do UNIQUE ở DB, trả thông điệp dễ hiểu
            String msg = "Phone hoặc Email đã tồn tại";
            if (dup.getMessage() != null && dup.getMessage().contains("phone")) msg = "Số điện thoại đã tồn tại";
            if (dup.getMessage() != null && dup.getMessage().contains("email")) msg = "Email đã tồn tại";
            throw new SQLException(msg, dup);
        }
    }

    /** Cập nhật thông tin khách hàng */
    public boolean updateCustomer(Customer c) throws SQLException {
        if (c.getId() == null) throw new IllegalArgumentException("Thiếu ID khách hàng");
        validateName(c.getName());
        validatePhone(c.getPhone());
        validateEmail(c.getEmail());
        try {
            return customerDAO.update(c);
        } catch (SQLIntegrityConstraintViolationException dup) {
            String msg = "Phone hoặc Email đã tồn tại";
            if (dup.getMessage() != null && dup.getMessage().contains("phone")) msg = "Số điện thoại đã tồn tại";
            if (dup.getMessage() != null && dup.getMessage().contains("email")) msg = "Email đã tồn tại";
            throw new SQLException(msg, dup);
        }
    }

    /** Xoá khách hàng theo ID */
    public boolean deleteCustomer(long id) throws SQLException {
        return customerDAO.delete(id);
    }

    /** Lấy chi tiết khách hàng */
    public Optional<Customer> getById(long id) throws SQLException {
        return customerDAO.findById(id);
    }

    /** Tìm kiếm + phân trang */
    public List<Customer> search(String keyword, int page, int pageSize) throws SQLException {
        return customerDAO.search(keyword, page, pageSize);
    }

    /** Tổng số dòng cho bộ lọc hiện tại (phục vụ Pagination) */
    public int count(String keyword) throws SQLException {
        return customerDAO.count(keyword);
    }

    /** Cộng/trừ điểm tích luỹ (delta âm hoặc dương) */
    public void adjustPoints(long customerId, int delta) throws SQLException {
        // Đơn giản: đọc hiện tại và cập nhật, đảm bảo không âm
        Customer c = customerDAO.findById(customerId).orElseThrow(() -> new SQLException("Không tìm thấy khách hàng"));
        int now = c.getPoints() == null ? 0 : c.getPoints();
        int next = Math.max(0, now + delta);
        c.setPoints(next);
        customerDAO.update(c);
    }

    // ===================== VALIDATION =====================
    private void validateName(String name) {
        if (isBlank(name) || name.trim().length() < 2)
            throw new IllegalArgumentException("Tên khách hàng không hợp lệ");
    }

    private void validateEmail(String email) {
        if (isBlank(email)) return; // cho phép trống
        if (!EMAIL_RE.matcher(email.trim()).matches())
            throw new IllegalArgumentException("Email không hợp lệ");
    }

    private void validatePhone(String phone) {
        if (isBlank(phone)) return; // cho phép trống
        if (!PHONE_RE.matcher(phone.trim()).matches())
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}