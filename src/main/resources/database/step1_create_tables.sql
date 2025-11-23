-- =============================================================
-- PHẦN 1: TẠO CÁC BẢNG
-- Chạy toàn bộ file này (Ctrl+Shift+Enter)
-- =============================================================
USE store_management;
-- 1. Supplier Debts Table (Accounts Payable)
DROP TABLE IF EXISTS supplier_debts;
CREATE TABLE supplier_debts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    amount DECIMAL(14, 2) NOT NULL,
    transaction_type ENUM('ADD_DEBT', 'PAY_DEBT', 'ADJUSTMENT') NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    CONSTRAINT fk_debt_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_debt_supplier_created (supplier_id, created_at)
) ENGINE = InnoDB;
-- 2. Budget Allocations Table
DROP TABLE IF EXISTS budget_allocations;
CREATE TABLE budget_allocations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    amount DECIMAL(14, 2) NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    CONSTRAINT fk_allocation_store FOREIGN KEY (store_id) REFERENCES stores(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_allocation_store_created (store_id, created_at)
) ENGINE = InnoDB;
-- 3. Budget Expenses Table
DROP TABLE IF EXISTS budget_expenses;
CREATE TABLE budget_expenses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    amount DECIMAL(14, 2) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    CONSTRAINT fk_expense_store FOREIGN KEY (store_id) REFERENCES stores(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_expense_store_created (store_id, created_at)
) ENGINE = InnoDB;