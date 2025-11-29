# 🏪 StoreManager — Phần mềm quản lý cửa hàng (Java + MySQL)

Tác giả:

**Phạm Ngọc Hưng** — MSSV: **20235342** 

**Bùi Trung Hoàng** - MSSV: **20235333**

Giảng viên hướng dẫn: **Trịnh Thành Trung**

Giáo viên phản biện: **Bùi Thị Mai Anh**

Ngôn ngữ chính: **Java**

Database: **MySQL**

Mục tiêu: Xây dựng một ứng dụng quản lý cửa hàng (đa cửa hàng, hàng hóa, khách hàng, báo cáo tài chính, đơn hàng) dễ hiểu, dễ chạy trên Windows và macOS, đồng thời dễ dàng mở rộng trong tương lai.

---

## 🎯 Tóm tắt chức năng chính

Ứng dụng hiện có các phân hệ chính:

1. **Quản lý khách hàng**

   - Thêm/sửa/xóa thông tin khách hàng.
   - Ghi nhận đơn hàng, điểm tích lũy.
   - Theo dõi lịch sử mua hàng.

2. **Quản lý kho & hàng hóa**

   - Quản lý số lượng hàng trong từng cửa hàng.
   - Cập nhật khi nhập/xuất hàng.
   - Quản lý danh sách nhà cung cấp.

3. **Quản lý tài chính**

   - Ghi nhận báo cáo thu/chi của từng cửa hàng.
   - Thêm/sửa/xóa báo cáo tài chính.
   - Tổng hợp chi phí đầu tư cơ sở vật chất.

Ghi chú: Phiên bản hiện tại tập trung vào chức năng lõi (CRUD, quản lý kho, đơn hàng, tài chính). Các tính năng mở rộng (như authentication, API REST, deploy cloud) được để trong phần "Hướng phát triển".

---
## Cấu trúc chi tiết — tệp nổi bật và mô tả ngắn

Dưới đây là mô tả ngắn cho các tệp quan trọng trong repo (file name → mục đích):

- Main.java — entry point (khởi tạo JavaFX application, load `main.fxml`).

- controller/\* — lớp điều khiển giao diện, mỗi controller chịu trách nhiệm cho một màn hình (ví dụ `CustomerController` xử lý CRUD khách hàng; `InventoryController` quản lý tồn kho; `FinanceController` xử lý báo cáo thu/chi; `LoginController` xử lý màn hình đăng nhập).

- dao/\* — Data Access Objects: kết nối, truy vấn, cập nhật dữ liệu trong database.

  - DBConnection.java: quản lý kết nối JDBC (đọc `db.properties`).
  - CustomerDAO, ProductDAO, OrderDAO, OrderEventDAO, FinanceDAO, ...: thao tác SQL với các bảng tương ứng.

- model/\* — các POJO đại diện cho thực thể trong hệ thống: `Customer`, `Product`, `Order`, `OrderItem`, `OrderEvent`, `FinanceReport`, `Store`, `Supplier`, `User`, `PointsLedger`.

- service/\* — business logic áp dụng rules giữa controllers và DAOs: `CustomerService`, `InventoryService`, `OrderService`, `FinanceService`, `SupplierService`.

- util/\* — helper/utility classes: `ValidationUtils`, `DateUtils`, `AlertUtils`, `PasswordUtils`, `Session`, `Migration` (migration script helper), `PointPolicy`.

- resources/fxml/\* — file giao diện JavaFX cho từng màn hình (một file FXML = một form / view).

- resources/database/\* — SQL scripts để khởi tạo database (`init.sql`, `step1_...`), tạo admin, cập nhật data, và `db.properties` để cấu hình kết nối trong môi trường dev.

- resources/css/style.css — phong cách giao diện JavaFX.

- resources/images/logostb.jpeg — logo dùng trong UI.

- src/test/java/com/example/AppTest.java — điểm bắt đầu cho unit test (JUnit) — repo có test đơn vị cơ bản.

Ghi chú: nếu bạn muốn tôi có thể mở rộng mô tả cho từng file (ví dụ thêm method/mô tả chi tiết bên trong DAO/Service), hoặc tạo bảng index file ↔ chức năng để phục vụ báo cáo thầy cô.

Thay `yourpassword` bằng mật khẩu MySQL của bạn.

### Bước 3: Build & chạy (Windows)

PowerShell (Windows):

```powershell
# Build
mvn clean package

# Chạy trực tiếp Java (ví dụ dùng plugin exec:java)
mvn -Dexec.mainClass="com.example.storemanagement.Main" -Dexec.cleanupDaemonThreads=false exec:java

# Nếu JavaFX cần module-path (nếu dùng JavaFX SDK local):
mvn -Dexec.mainClass="com.example.storemanagement.Main" -Dexec.args="--module-path $env:PATH_TO_FX --add-modules javafx.controls,javafx.fxml" exec:java
```

### Bước 4: Chạy ứng dụng

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.example.storemanagement.Main" -f pom.xml
```

### Bước tương tự cho macOS / Linux (zsh / bash)

```bash
# Build
mvn clean package

# Chạy
mvn -Dexec.mainClass="com.example.storemanagement.Main" exec:java

# Nếu dùng JavaFX SDK (đặt PATH_TO_FX trên macOS vào shell)
export PATH_TO_FX=/path/to/javafx/lib
mvn -Dexec.mainClass="com.example.storemanagement.Main" -Dexec.args="--module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml" exec:java
```

---

## 🧠 Kiến trúc & nguyên tắc chính

- Áp dụng **MVC (Model–View–Controller)** tách biệt rõ logic, giao diện và dữ liệu.
- Code chuẩn **DAO + Service layer** để dễ mở rộng.
- Mọi thực thể (Customer, Product, FinanceReport...) đều có CRUD đầy đủ.
- Kết nối DB qua **JDBC**, tách cấu hình khỏi code.
- Dễ dàng thêm module mới (ví dụ sau này thêm LoginController, ReportModule, ...).

## 🧩 Hướng phát triển & mở rộng trong tương lai

Đây là một số ý tưởng cải tiến để nâng cao chất lượng và khả năng mở rộng của hệ thống:

- Authentication & Authorization (Login, Role-based access)
- Đổi sang Spring Boot + Spring Data JPA (Hibernate) để giảm boilerplate cho DAO
- Tách backend/REST API (Spring Boot) và frontend (modular JavaFX hoặc SPA web) để hỗ trợ nhiều client
- Containerization: Docker + Docker Compose cho MySQL + app để dễ triển khai
- Triển khai CI/CD (GitHub Actions) + test tự động và build artifact
- Export/Reports: PDF/Excel + visualization dashboards (grafana/quick charts)
- Notifications: tích hợp email / SMS khi đơn mới hoặc báo cáo
- Multi-tenant & multi-store orchestration, đồng bộ kho theo cửa hàng
- Internationalization (i18n) — hỗ trợ đa ngôn ngữ
