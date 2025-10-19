package com.example.storemanagement.controller;

/*
 * InventoryController – Controller gắn với inventory.fxml
 * 🎯 Nhiệm vụ:
 *   - Hiển thị tổng quan hàng tồn kho (từ view v_inventory_overview trong DB).
 *   - Cho phép tìm kiếm, lọc theo cửa hàng, phân trang, CRUD sản phẩm.
 *   - Thực hiện nhập kho / xuất kho (Import / Export stock).
 * 💡 Ghi chú:
 *   - Tạm thời truy vấn danh sách Store và Supplier trực tiếp từ DB (chưa tạo DAO riêng).
 *   - Sau này có thể tách thành StoreDAO, SupplierDAO để đúng mô hình MVC.
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.storemanagement.dao.DBConnection;
import com.example.storemanagement.dao.ProductDAO;
import com.example.storemanagement.model.Product;
import com.example.storemanagement.service.InventoryService;
import com.example.storemanagement.util.AlertUtils;
import com.example.storemanagement.util.DateUtils;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;

public class InventoryController {

    // ====== Tham chiếu các phần tử FXML theo fx:id trong inventory.fxml ======
    @FXML private ComboBox<IdName> storeFilter;        // Lọc theo cửa hàng
    @FXML private ComboBox<IdName> supplierFilter;     // Lọc theo nhà cung cấp
    @FXML private TextField productSearchField;        // Ô tìm kiếm sản phẩm
    @FXML private Button searchProductBtn;             // Nút tìm kiếm
    @FXML private Button addProductBtn;                // Nút thêm sản phẩm
    @FXML private Button editProductBtn;               // Nút sửa sản phẩm
    @FXML private Button deleteProductBtn;             // Nút xóa sản phẩm
    @FXML private Button importStockBtn;               // Nút nhập kho
    @FXML private Button exportStockBtn;               // Nút xuất kho
    @FXML private Button refreshInventoryBtn;          // Nút làm mới danh sách

    @FXML private TableView<ProductDAO.InventoryOverview> inventoryTable; // Bảng dữ liệu kho
    @FXML private TableColumn<ProductDAO.InventoryOverview, Long> colId;  // Cột ID sản phẩm
    @FXML private TableColumn<ProductDAO.InventoryOverview, String> colName; // Cột tên
    @FXML private TableColumn<ProductDAO.InventoryOverview, String> colSku;  // Cột mã SKU
    @FXML private TableColumn<ProductDAO.InventoryOverview, String> colStore; // Cột cửa hàng
    @FXML private TableColumn<ProductDAO.InventoryOverview, String> colSupplier; // Cột nhà cung cấp
    @FXML private TableColumn<ProductDAO.InventoryOverview, Integer> colStock;   // Cột số lượng tồn
    @FXML private TableColumn<ProductDAO.InventoryOverview, BigDecimal> colUnitPrice; // Đơn giá
    @FXML private TableColumn<ProductDAO.InventoryOverview, String> colUpdatedAt;     // Ngày cập nhật

    @FXML private Label inventoryStatusLabel;          // Nhãn trạng thái
    @FXML private Pagination inventoryPagination;      // Phân trang

    // ====== Biến xử lý logic nghiệp vụ ======
    private final InventoryService inventoryService = new InventoryService(); // Gọi xuống tầng Service
    private final int pageSize = 20;      // Số dòng hiển thị mỗi trang
    private int currentPage = 1;          // Trang hiện tại
    private String currentKeyword = "";   // Từ khóa tìm kiếm hiện tại
    private Long currentStoreId = null;   // ID cửa hàng được chọn (lọc)

    // ====== Khởi tạo giao diện sau khi FXML load xong ======
    @FXML
    public void initialize() {
        // Nạp danh sách cửa hàng và nhà cung cấp vào combobox
        storeFilter.setItems(FXCollections.observableArrayList(loadStores()));
        supplierFilter.setItems(FXCollections.observableArrayList(loadSuppliers()));

        // Cấu hình hiển thị từng cột trong TableView
        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().productId));
        colName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().productName));
        colSku.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().sku));
        colStore.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().storeName));
        colSupplier.setCellValueFactory(cd -> new ReadOnlyStringWrapper("")); // chưa join supplier
        colStock.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().quantity));
        colUnitPrice.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(null)); // chưa hiển thị giá
        colUpdatedAt.setCellValueFactory(cd -> new ReadOnlyStringWrapper(DateUtils.format(cd.getValue().updatedAt)));

        // ====== Gán sự kiện các nút ======
        searchProductBtn.setOnAction(e -> doSearch(1));            // tìm kiếm
        refreshInventoryBtn.setOnAction(e -> doSearch(currentPage)); // làm mới
        storeFilter.valueProperty().addListener((obs, o, n) -> {    // lọc theo cửa hàng
            currentStoreId = (n == null ? null : n.id);
            doSearch(1);
        });

        addProductBtn.setOnAction(e -> onAddProduct());             // thêm sản phẩm
        editProductBtn.setOnAction(e -> onEditProduct());           // sửa sản phẩm
        deleteProductBtn.setOnAction(e -> onDeleteProduct());       // xóa sản phẩm

        importStockBtn.setOnAction(e -> onMoveStock(true));         // nhập kho
        exportStockBtn.setOnAction(e -> onMoveStock(false));        // xuất kho

        // Khi đổi trang → nạp dữ liệu mới
        inventoryPagination.currentPageIndexProperty().addListener((obs, ov, nv) -> {
            currentPage = nv.intValue() + 1;
            doSearch(currentPage);
        });

        // Lần đầu load
        doSearch(1);
    }

    // ====== Hàm tìm kiếm và hiển thị danh sách tồn kho ======
    private void doSearch(int page) {
        try {
            currentKeyword = productSearchField.getText() == null ? "" : productSearchField.getText().trim();
            List<ProductDAO.InventoryOverview> list =
                    inventoryService.getInventoryOverview(currentStoreId, currentKeyword, page, pageSize);

            // Ước lượng số trang: nếu ít hơn pageSize và là trang đầu tiên → chỉ 1 trang
            int pageCount = list.size() < pageSize && page == 1 ? 1 : Math.max(1, page);
            inventoryPagination.setPageCount(pageCount);
            inventoryPagination.setCurrentPageIndex(page - 1);

            // Cập nhật bảng
            inventoryTable.getItems().setAll(list);
            updateStatus();
        } catch (SQLException ex) {
            AlertUtils.error("Load inventory failed", ex.getMessage());
        }
    }

    // ====== Nhập / Xuất kho ======
    private void onMoveStock(boolean isImport) {
        ProductDAO.InventoryOverview sel = inventoryTable.getSelectionModel().getSelectedItem();
        if (sel == null) { // chưa chọn dòng nào
            AlertUtils.warn("No selection", "Chọn một dòng kho để " + (isImport ? "nhập" : "xuất"));
            return;
        }

        // Hộp thoại nhập số lượng
        TextInputDialog d = new TextInputDialog("1");
        d.setTitle(isImport ? "Import Stock" : "Export Stock");
        d.setHeaderText("Số lượng");
        d.setContentText("Quantity:");
        Optional<String> res = d.showAndWait();

        // Nếu người dùng bấm OK
        res.ifPresent(s -> {
            try {
                int qty = Integer.parseInt(s.trim());
                if (qty <= 0) throw new NumberFormatException(); // kiểm tra hợp lệ
                if (isImport)
                    inventoryService.importStock(sel.storeId, sel.productId, qty, "Manual import");
                else
                    inventoryService.exportStock(sel.storeId, sel.productId, qty, "Manual export");
                doSearch(currentPage); // cập nhật lại bảng
            } catch (NumberFormatException ne) {
                AlertUtils.warn("Giá trị không hợp lệ", "Quantity phải là số nguyên dương");
            } catch (SQLException ex) {
                AlertUtils.error("Stock move failed", ex.getMessage());
            }
        });
    }

    // ====== Thêm sản phẩm mới ======
    private void onAddProduct() {
        Dialog<Product> d = buildProductDialog(null); // mở dialog rỗng
        d.showAndWait().ifPresent(p -> { // nếu người dùng bấm OK
            try {
                long id = inventoryService.addProduct(p); // thêm DB
                AlertUtils.info("Created", "Đã tạo sản phẩm #" + id);
                doSearch(1);
            } catch (SQLException ex) {
                AlertUtils.error("Create failed", ex.getMessage());
            }
        });
    }

    // ====== Sửa sản phẩm ======
    private void onEditProduct() {
        ProductDAO.InventoryOverview sel = inventoryTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtils.warn("No selection", "Chọn một dòng để sửa sản phẩm"); return; }
        try {
            Product p = inventoryService.findProductById(sel.productId).orElse(null);
            if (p == null) { AlertUtils.warn("Not found", "Không tìm thấy sản phẩm"); return; }

            Dialog<Product> d = buildProductDialog(p);
            d.showAndWait().ifPresent(updated -> {
                try {
                    updated.setId(p.getId());
                    inventoryService.updateProduct(updated);
                    doSearch(currentPage);
                } catch (SQLException ex) { AlertUtils.error("Update failed", ex.getMessage()); }
            });
        } catch (SQLException ex) {
            AlertUtils.error("Load product failed", ex.getMessage());
        }
    }

    // ====== Xóa sản phẩm ======
    private void onDeleteProduct() {
        ProductDAO.InventoryOverview sel = inventoryTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtils.warn("No selection", "Chọn một sản phẩm để xoá"); return; }
        if (!AlertUtils.confirm("Xác nhận", "Xoá sản phẩm #" + sel.productId + "?")) return;
        try {
            inventoryService.deleteProduct(sel.productId);
            doSearch(currentPage);
        } catch (SQLException ex) {
            AlertUtils.error("Delete failed", ex.getMessage());
        }
    }

    // ====== Hộp thoại thêm / sửa sản phẩm ======
    private Dialog<Product> buildProductDialog(Product init) {
        Dialog<Product> d = new Dialog<>();
        d.setTitle(init == null ? "Thêm sản phẩm" : "Sửa sản phẩm");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Tạo các ô nhập liệu
        TextField tfSku = new TextField(init != null ? init.getSku() : "");
        TextField tfName = new TextField(init != null ? init.getName() : "");
        TextField tfUnit = new TextField(init != null ? init.getUnit() : "pcs");
        TextField tfPrice = new TextField(init != null && init.getUnitPrice()!=null? init.getUnitPrice().toPlainString() : "0");
        TextField tfCost  = new TextField(init != null && init.getUnitCost()!=null ? init.getUnitCost().toPlainString() : "0");
        CheckBox cbActive = new CheckBox("Active"); cbActive.setSelected(init == null || init.isActive());

        // Dàn layout theo dạng lưới
        GridPane gp = new GridPane(); gp.setHgap(8); gp.setVgap(8);
        gp.addRow(0, new Label("SKU"), tfSku);
        gp.addRow(1, new Label("Name"), tfName);
        gp.addRow(2, new Label("Unit"), tfUnit);
        gp.addRow(3, new Label("Unit Price"), tfPrice);
        gp.addRow(4, new Label("Unit Cost"), tfCost);
        gp.addRow(5, new Label("Status"), cbActive);
        d.getDialogPane().setContent(gp);

        // Chuyển giá trị người nhập thành đối tượng Product
        d.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    Product p = new Product();
                    p.setSku(tfSku.getText());
                    p.setName(tfName.getText());
                    p.setUnit(tfUnit.getText());
                    p.setUnitPrice(new BigDecimal(tfPrice.getText().trim()));
                    p.setUnitCost(new BigDecimal(tfCost.getText().trim()));
                    p.setActive(cbActive.isSelected());
                    return p;
                } catch (Exception ex) {
                    AlertUtils.warn("Giá trị không hợp lệ", "Giá/Cost phải là số hợp lệ");
                    return null;
                }
            }
            return null;
        });
        return d;
    }

    // ====== Cập nhật trạng thái phía dưới bảng ======
    private void updateStatus() {
        int selected = inventoryTable.getSelectionModel().getSelectedIndices().size();
        int total = inventoryTable.getItems().size();
        inventoryStatusLabel.setText(total + " item(s) • " + selected + " selected");
    }

    // ====== Nạp danh sách cửa hàng từ DB ======
    private List<IdName> loadStores() {
        List<IdName> list = new ArrayList<>();
        String sql = "SELECT id, name FROM stores ORDER BY name";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new IdName(rs.getLong(1), rs.getString(2)));
        } catch (SQLException ignored) {}
        return list;
    }

    // ====== Nạp danh sách nhà cung cấp từ DB ======
    private List<IdName> loadSuppliers() {
        List<IdName> list = new ArrayList<>();
        String sql = "SELECT id, name FROM suppliers ORDER BY name";
        try (Connection cn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new IdName(rs.getLong(1), rs.getString(2)));
        } catch (SQLException ignored) {}
        return list;
    }

    // ====== Lớp phụ trợ nhỏ (id + name) dùng cho ComboBox ======
    public static class IdName {
        public final long id;   // ID đối tượng (store hoặc supplier)
        public final String name; // Tên hiển thị
        public IdName(long id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name + " (#" + id + ")"; } // hiển thị đẹp trong combobox
    }
}
