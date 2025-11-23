-- 1) Cập nhật bảng stores: thêm cột type
ALTER TABLE stores
ADD COLUMN type ENUM('CENTRAL', 'RETAIL') NOT NULL DEFAULT 'RETAIL'
AFTER name;
-- Đặt S001 làm Kho Tổng (CENTRAL)
UPDATE stores
SET type = 'CENTRAL'
WHERE code = 'S001';
-- 2) Cập nhật bảng users: mở rộng ENUM role
-- Lưu ý: MySQL không hỗ trợ trực tiếp thêm giá trị vào giữa ENUM dễ dàng,
-- ta dùng MODIFY COLUMN để định nghĩa lại toàn bộ danh sách.
ALTER TABLE users
MODIFY COLUMN role ENUM(
        'admin',
        'user',
        'seller',
        'logistic',
        'accountant'
    ) NOT NULL DEFAULT 'user';
-- 3) Tạo user mẫu cho bộ phận Logistic (để test)
INSERT INTO users (username, password, role)
VALUES (
        'logistic_user',
        '$2a$10$X/hX9.Q/hX9.Q/hX9.Q/hX9.Q/hX9.Q/hX9.Q/hX9.Q/hX9.Q/hX9',
        'logistic'
    );
-- (Mật khẩu mẫu ở trên là placeholder, thực tế bạn nên tạo qua giao diện Register hoặc dùng hash chuẩn)
-- Mật khẩu '123456' hash BCrypt: $2a$10$6j.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q.q
-- Để tiện, ta dùng câu lệnh update sau khi insert nếu muốn set pass cụ thể,
-- hoặc user tự đăng ký rồi admin sửa role.