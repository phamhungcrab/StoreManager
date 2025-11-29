# Hướng dẫn vẽ biểu đồ gói & lớp (phiên bản thủ công)

Tài liệu này liệt kê các gói chính trong dự án, các lớp bên trong, và gợi ý quan hệ để bạn tự vẽ biểu đồ thiết kế bằng bất kỳ công cụ trực quan nào (Mermaid, PlantUML, hoặc vẽ tay). Mỗi mục đều gợi ý loại đường nối (phụ thuộc, kết hợp/kết tập/hợp thành, kế thừa, thực thi) cho biểu đồ rõ ràng, đúng yêu cầu bài tập.

## 1) Gói khởi động & phiên làm việc (`com.example.storemanagement`)
- **Lớp:** `Main` (khởi động JavaFX, tải giao diện login), `MainController` (điều hướng chính), `Session` (lưu trạng thái đăng nhập tĩnh).
- **Quan hệ gợi ý:**
  - `Main` → `MainController` (phụ thuộc: tạo/khởi động controller qua FXML).
  - `MainController` → `Session` (phụ thuộc: đọc/ghi thông tin đăng nhập để bật/tắt chức năng).

## 2) Gói giao diện (controllers)
- **Lớp:** `LoginController`, `RegisterController`, `MainController`, `CustomerController`, `OrderController`, `InventoryController`, `SupplierController`, `FinanceController`, `AccountantController`, `SellerBudgetController`, `OrderLogController`, `UserController`, `LogisticsController`.
- **Quan hệ gợi ý:**
  - Mỗi controller **phụ thuộc** vào `Session` để kiểm tra quyền.
  - Mỗi controller **phụ thuộc** vào service tương ứng (xem mục 3) để thực thi nghiệp vụ.
  - `MainController` có **kết hợp** (association) đến các controller màn hình con qua FXML loader (nếu muốn thể hiện điều hướng).

## 3) Gói nghiệp vụ (services)
- **Lớp:** `CustomerService`, `OrderService`, `InventoryService`, `SupplierService`, `FinanceService`.
- **Quan hệ gợi ý:**
  - Mỗi service **kết hợp** (aggregation) với DAO tương ứng (một service chứa/giữ tham chiếu DAO):
    - `CustomerService` → `CustomerDAO`
    - `OrderService` → `OrderDAO`, `OrderEventDAO`
    - `InventoryService` → `ProductDAO`, `SupplierProductPriceDAO`
    - `SupplierService` → `SupplierDAO`, `SupplierDebtDAO`
    - `FinanceService` → `FinanceDAO`, `BudgetAllocationDAO`, `BudgetExpenseDAO`
  - Các service **phụ thuộc** vào `Session` (đọc `storeId` hoặc role nếu cần lọc dữ liệu).

## 4) Gói truy cập dữ liệu (dao)
- **Lớp:** `DBConnection` (singleton), `CustomerDAO`, `OrderDAO`, `OrderEventDAO`, `ProductDAO`, `SupplierProductPriceDAO`, `SupplierDAO`, `SupplierDebtDAO`, `FinanceDAO`, `BudgetAllocationDAO`, `BudgetExpenseDAO`, `StoreDAO`, `UserDAO`.
- **Quan hệ gợi ý:**
  - Mọi DAO **kết hợp** (composition) với `DBConnection` (DAO sở hữu connection chung/singleton để truy vấn DB).
  - Mỗi DAO **kết hợp** với model tương ứng (ví dụ `CustomerDAO` ↔ `Customer`, `OrderDAO` ↔ `Order`/`OrderItem`).

## 5) Gói mô hình miền (model)
- **Lớp:** `Customer`, `Order`, `OrderItem`, `Product`, `Supplier`, `SupplierProductPrice`, `SupplierDebt`, `Store`, `User`, `FinanceReport`, `BudgetAllocation`, `BudgetExpense`, `OrderEvent`, `PointsLedger`.
- **Quan hệ gợi ý (association/composition):**
  - `Order` —< `OrderItem` (hợp thành: đời sống `OrderItem` gắn với `Order`).
  - `OrderItem` → `Product` (kết hợp: mỗi dòng hàng tham chiếu sản phẩm).
  - `Order` → `Customer`, `Order` → `Store` (kết hợp: đơn thuộc khách hàng và cửa hàng).
  - `OrderEvent` → `Order` (kết hợp: log trạng thái theo đơn).
  - `SupplierProductPrice` → `Supplier`, `SupplierProductPrice` → `Product` (kết hợp giá theo nhà cung cấp & sản phẩm).
  - `SupplierDebt` → `Supplier` (kết hợp: khoản nợ gắn nhà cung cấp).
  - `BudgetAllocation` —< `BudgetExpense` (hợp thành: chi tiêu thuộc một phân bổ).
  - `FinanceReport` → `BudgetAllocation`/`BudgetExpense` (phụ thuộc: báo cáo tổng hợp dữ liệu tài chính).
  - `PointsLedger` → `Customer` (kết hợp: nhật ký điểm của khách hàng).

## 6) Gói tiện ích (util)
- **Lớp:** `PasswordUtils`, `ValidationUtils`, `DateUtils`, `AlertUtils`, `Migration`, `PointPolicy`.
- **Quan hệ gợi ý:**
  - Controllers và services **phụ thuộc** vào các tiện ích này (đường nét đứt dependency).
  - `Migration`/`PointPolicy` cũng **phụ thuộc** vào DAO hoặc model khi áp dụng chính sách.

## Cách chia sơ đồ để dễ đọc (đề xuất)
1. **Biểu đồ tổng quan gói**: một hộp cho mỗi gói (controller, service, dao, model, util, app) và đường phụ thuộc từ UI → service → DAO → model/DB.
2. **Biểu đồ gói controller**: chỉ hộp controller và mũi tên dependency tới service + Session; thêm association `MainController` → các màn hình con nếu muốn.
3. **Biểu đồ gói service/DAO**: service nối DAO (aggregation), DAO nối `DBConnection` (composition), DAO association với model.
4. **Biểu đồ gói model**: tập trung vào quan hệ thực thể (composition/association) như `Order`—`OrderItem`, `BudgetAllocation`—`BudgetExpense`.
5. **Biểu đồ util**: dependency từ controller/service sang các helper (đường nét đứt), giữ riêng để không rối sơ đồ chính.

## Ghi chú ký hiệu (áp dụng cho mọi biểu đồ)
- **Dependency (phụ thuộc):** mũi tên nét đứt `A ..> B` khi A chỉ gọi/chỉ dùng tạm thời B.
- **Association/Kết hợp:** mũi tên nét liền `A --> B` nếu A giữ tham chiếu B.
- **Aggregation (kết tập):** `A o-- B` khi A chứa B nhưng vòng đời B độc lập (service chứa DAO singleton).
- **Composition (hợp thành):** `A *-- B` khi B sống cùng vòng đời A (`Order` chứa `OrderItem`).
- **Inheritance/Implementation:** `A --|> B` (kế thừa) hoặc `A ..|> B` (thực thi interface). Dự án hiện không dùng nhiều kế thừa, nhưng ký hiệu vẫn cần nếu bạn mở rộng.

Với danh sách lớp và gợi ý quan hệ trên, bạn có thể ghép các biểu đồ nhỏ (từng gói) hoặc một sơ đồ tổng hợp theo đúng yêu cầu bài tập mà không bị rối.
