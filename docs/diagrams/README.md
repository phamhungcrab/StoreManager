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
```bash
plantuml docs/diagrams/overview.puml
```
