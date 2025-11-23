-- HƯỚNG DẪN:
-- 1. Đăng ký tài khoản mới trong ứng dụng với tên đăng nhập là 'admin' (mật khẩu tuỳ ý).
-- 2. Chạy script này để cấp quyền Admin cho tài khoản đó.
UPDATE users
SET role = 'admin'
WHERE username = 'admin';