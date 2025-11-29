
# Sơ đồ lớp (Mermaid & PlantUML)
Các sơ đồ được chia nhỏ theo từng nhóm để dễ quan sát. Phiên bản Mermaid được đơn giản hóa (ít đường hơn) giúp xem nhanh trên GitHub, còn PlantUML giữ chi tiết gốc nếu cần tra cứu sâu.

## 1. Tổng quan kiến trúc
- **Tệp Mermaid:** `overview.mmd`
- **Tệp PlantUML:** `overview.puml`
- **Nội dung:** Luồng chính từ giao diện → service → DAO → DB, cùng các model lõi.

## 2. Tầng giao diện (Controller)
- **Tệp Mermaid:** `controllers.mmd`
- **Tệp PlantUML:** `controllers.puml`
- **Nội dung:** Các controller JavaFX và phụ thuộc trực tiếp (service, DAO, tiện ích, phiên đăng nhập).

## 3. Tầng nghiệp vụ & dữ liệu
- **Tệp Mermaid:** `services_dao.mmd`
- **Tệp PlantUML:** `services_dao.puml`
- **Nội dung:** Quan hệ giữa Service, DAO, singleton `DBConnection`, và các POJO mà DAO ánh xạ.

## 4. Mô hình miền
- **Tệp Mermaid:** `models.mmd`
- **Tệp PlantUML:** `models.puml`
- **Nội dung:** Quan hệ giữa các POJO: đơn hàng, dòng hàng, khách hàng, sản phẩm, nhà cung cấp, báo cáo tài chính, điểm thưởng, người dùng.

## Cách render Mermaid
Bạn có thể xem trực tiếp trên GitHub hoặc xuất ảnh bằng CLI (ví dụ dùng `mmdc`):
```bash
mmdc -i docs/diagrams/overview.mmd -o overview.png
```

## Cách render PlantUML
Giữ nguyên hướng dẫn cũ nếu bạn cần nhiều chi tiết:
=======
# Sơ đồ lớp PlantUML
Các tệp `.puml` dưới đây chia nhỏ sơ đồ lớp theo từng nhóm để dễ quan sát. Mỗi sơ đồ chỉ hiển thị tên lớp và quan hệ chính (phụ thuộc, kết hợp/kết tập, hợp thành) đúng như mã nguồn.

## 1. Tổng quan kiến trúc
- **Tệp:** `overview.puml`
- **Nội dung:** Các package chính (App/Session, Controllers, Services, DAOs, Domain Models, Utilities) và luồng phụ thuộc: Controller → Service → DAO → DBConnection → Model.
- **Mục đích:** Cho cái nhìn nhanh về cách các tầng giao tiếp với nhau từ giao diện JavaFX xuống cơ sở dữ liệu.

## 2. Tầng giao diện (Controller)
- **Tệp:** `controllers.puml`
- **Nội dung:** Các controller JavaFX và các phụ thuộc trực tiếp của chúng (Service, DBConnection, UserDAO, Session, PasswordUtils, AlertUtils, DateUtils).
- **Mục đích:** Thể hiện rõ controller nào dùng service nào, trường hợp nào truy cập DB trực tiếp, và các tiện ích hỗ trợ xác thực/phiên làm việc.

## 3. Tầng nghiệp vụ & truy cập dữ liệu
- **Tệp:** `services_dao.puml`
- **Nội dung:** Quan hệ giữa Service và DAO, DAO với DBConnection, cùng các POJO mà mỗi DAO ánh xạ.
- **Mục đích:** Làm rõ ranh giới nghiệp vụ–dữ liệu và điểm kết nối tới singleton `DBConnection`.

## 4. Mô hình miền
- **Tệp:** `models.puml`
- **Nội dung:** Quan hệ giữa các POJO: đơn hàng, dòng hàng, khách hàng, sản phẩm, nhà cung cấp, báo cáo tài chính, điểm thưởng, người dùng.
- **Mục đích:** Nhìn nhanh cấu trúc dữ liệu và mức độ kết hợp/hợp thành giữa các thực thể.

## Cách dùng
Mỗi tệp có thể được dựng bằng PlantUML CLI hoặc plugin IDE. Ví dụ:
