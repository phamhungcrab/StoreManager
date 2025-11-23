-- 1. Thêm cột store_id vào bảng users để liên kết nhân viên với cửa hàng
ALTER TABLE users
ADD COLUMN store_id BIGINT NULL
AFTER role;
ALTER TABLE users
ADD CONSTRAINT fk_users_store FOREIGN KEY (store_id) REFERENCES stores(id) ON UPDATE CASCADE ON DELETE
SET NULL;
-- 2. Thêm cột cash_balance vào bảng stores để quản lý quỹ tiền mặt (Petty Cash)
ALTER TABLE stores
ADD COLUMN cash_balance DECIMAL(14, 2) NOT NULL DEFAULT 0.00
AFTER phone;