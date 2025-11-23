-- =============================================================
-- PHẦN 4: TEST DATA
-- Chạy toàn bộ file này (Ctrl+Shift+Enter)
-- =============================================================
USE store_management;
-- Test: Allocate budget to Store 1
INSERT INTO budget_allocations (store_id, amount, note, created_by)
VALUES (1, 10000000, 'Cấp quỹ tháng 11/2025', 'admin');
-- Test: Add debt to Supplier 1
INSERT INTO supplier_debts (
        supplier_id,
        amount,
        transaction_type,
        note,
        created_by
    )
VALUES (
        1,
        5000000,
        'ADD_DEBT',
        'Nhập hàng tháng 11',
        'admin'
    );
-- Test: Seller records expense
INSERT INTO budget_expenses (store_id, amount, reason, note, created_by)
VALUES (
        1,
        500000,
        'Thay bóng đèn',
        'Bóng đèn LED khu vực bán hàng',
        'seller_user'
    );
-- Verify results
SELECT 'Store Cash Balance:' AS description,
    cash_balance
FROM stores
WHERE id = 1;
SELECT 'Supplier 1 Total Debt:' AS description,
    SUM(amount) AS total_debt
FROM supplier_debts
WHERE supplier_id = 1;