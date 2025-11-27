package com.example.storemanagement.controller;

import com.example.storemanagement.dao.DBConnection;
import com.example.storemanagement.dao.ProductDAO;
import com.example.storemanagement.model.Store;
import com.example.storemanagement.service.InventoryService;
import com.example.storemanagement.util.AlertUtils;
import com.example.storemanagement.util.DateUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LogisticsController {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<ProductDAO.InventoryOverview> inventoryTable;
    @FXML
    private TableColumn<ProductDAO.InventoryOverview, String> colStoreName;
    @FXML
    private TableColumn<ProductDAO.InventoryOverview, String> colStoreType;
    @FXML
    private TableColumn<ProductDAO.InventoryOverview, String> colProductName;
    @FXML
    private TableColumn<ProductDAO.InventoryOverview, String> colSku;
    @FXML
    private TableColumn<ProductDAO.InventoryOverview, Integer> colQuantity;
    @FXML
    private TableColumn<ProductDAO.InventoryOverview, String> colUpdatedAt;

    @FXML
    private TextField tfSelectedProduct;
    @FXML
    private ComboBox<Store> cbFromStore;
    @FXML
    private ComboBox<Store> cbToStore;
    @FXML
    private TextField tfQuantity;

    private final InventoryService inventoryService = new InventoryService();
    private ProductDAO.InventoryOverview selectedItem;
    private List<Store> stores; // holds all stores loaded from DB

    @FXML
    public void initialize() {
        // Init columns
        colStoreName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().storeName));
        colStoreType.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().storeType));
        colProductName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().productName));
        colSku.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().sku));
        colQuantity.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().quantity));
        colUpdatedAt.setCellValueFactory(cd -> new ReadOnlyStringWrapper(DateUtils.format(cd.getValue().updatedAt)));

        // Load stores from DB
        stores = loadStores();

        // Populate combo boxes with all stores
        cbFromStore.getItems().setAll(stores);
        cbToStore.getItems().setAll(stores);

        // Converter for displaying store name and type
        StringConverter<Store> storeConverter = new StringConverter<>() {
            @Override
            public String toString(Store s) {
                return s == null ? "" : s.getName() + " (" + s.getType() + ")";
            }

            @Override
            public Store fromString(String string) {
                return null; // not needed for combo box selection
            }
        };
        cbFromStore.setConverter(storeConverter);
        cbToStore.setConverter(storeConverter);

        // Auto-select first store if available
        if (!stores.isEmpty()) {
            cbFromStore.getSelectionModel().select(0);
            if (stores.size() > 1) {
                cbToStore.getSelectionModel().select(1);
            } else {
                cbToStore.getSelectionModel().select(0);
            }
        }

        // Table selection listener
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selectedItem = val;
            if (val != null) {
                tfSelectedProduct.setText(val.productName + " (" + val.sku + ")");
                // Auto-select the store of the selected item as "From" if it matches a central
                // store
                Store s = stores.stream()
                        .filter(st -> st.getId().equals(val.storeId))
                        .findFirst()
                        .orElse(null);
                if (s != null) {
                    cbFromStore.getSelectionModel().select(s);
                }
            } else {
                tfSelectedProduct.setText("");
            }
        });

        loadData();
        initPriceTable();
    }

    @FXML
    private void onSearch() {
        loadData();
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        loadData();
    }

    private void loadData() {
        try {
            String kw = searchField.getText();
            List<ProductDAO.InventoryOverview> list = inventoryService.getInventoryOverview(null, null, kw, 1, 1000);
            inventoryTable.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            AlertUtils.error("Error", "Failed to load inventory: " + e.getMessage());
        }
    }

    @FXML
    private void onTransfer() {
        if (selectedItem == null) {
            AlertUtils.warn("Chưa chọn sản phẩm", "Vui lòng chọn một dòng sản phẩm từ bảng trên.");
            return;
        }
        Store from = cbFromStore.getValue();
        Store to = cbToStore.getValue();
        if (from == null || to == null) {
            AlertUtils.warn("Chưa chọn kho", "Vui lòng chọn kho xuất và kho nhập.");
            return;
        }
        if (from.getId().equals(to.getId())) {
            AlertUtils.warn("Lỗi", "Kho xuất và kho nhập phải khác nhau.");
            return;
        }
        try {
            int qty = Integer.parseInt(tfQuantity.getText().trim());
            if (qty <= 0) {
                throw new NumberFormatException();
            }
            inventoryService.transferStock(from.getId(), to.getId(), selectedItem.productId, qty, "Logistics Transfer");
            AlertUtils.info("Thành công",
                    "Đã chuyển " + qty + " sản phẩm từ " + from.getName() + " sang " + to.getName());
            loadData();
            tfQuantity.clear();
        } catch (NumberFormatException e) {
            AlertUtils.warn("Lỗi", "Số lượng phải là số nguyên dương.");
        } catch (SQLException e) {
            AlertUtils.error("Lỗi", "Chuyển kho thất bại: " + e.getMessage());
        }
    }

    private List<Store> loadStores() {
        List<Store> list = new ArrayList<>();
        String sql = "SELECT id, code, name, type, address, phone, created_at FROM stores ORDER BY type, name";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Store s = new Store();
                s.setId(rs.getLong("id"));
                s.setCode(rs.getString("code"));
                s.setName(rs.getString("name"));
                s.setType(rs.getString("type"));
                s.setAddress(rs.getString("address"));
                s.setPhone(rs.getString("phone"));
                // timestamp handling omitted for brevity
                list.add(s);
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    @FXML
    private void onAddSupplier() {
        AlertUtils.info("Tính năng đang phát triển", "Chức năng thêm nhà cung cấp sẽ sớm ra mắt.");
    }

    @FXML
    private void onEditSupplier() {
        AlertUtils.info("Tính năng đang phát triển", "Chức năng sửa nhà cung cấp sẽ sớm ra mắt.");
    }

    @FXML
    private void onDeleteSupplier() {
        AlertUtils.info("Tính năng đang phát triển", "Chức năng xóa nhà cung cấp sẽ sớm ra mắt.");
    }

    // ================= Import Price Management =================
    @FXML
    private TableView<com.example.storemanagement.model.SupplierProductPrice> priceTable;
    @FXML
    private TableColumn<com.example.storemanagement.model.SupplierProductPrice, String> colPriceProduct;
    @FXML
    private TableColumn<com.example.storemanagement.model.SupplierProductPrice, String> colPriceSku;
    @FXML
    private TableColumn<com.example.storemanagement.model.SupplierProductPrice, String> colPriceSupplier;
    @FXML
    private TableColumn<com.example.storemanagement.model.SupplierProductPrice, String> colPriceValue;
    @FXML
    private TableColumn<com.example.storemanagement.model.SupplierProductPrice, String> colPriceUpdated;
    @FXML
    private TextField searchPriceField;

    private final com.example.storemanagement.dao.SupplierProductPriceDAO priceDAO = new com.example.storemanagement.dao.SupplierProductPriceDAO();
    private final com.example.storemanagement.dao.SupplierDAO supplierDAO = new com.example.storemanagement.dao.SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private void initPriceTable() {
        colPriceProduct.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getProductName()));
        colPriceSku.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getSku()));
        colPriceSupplier.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getSupplierName()));
        colPriceValue.setCellValueFactory(
                cd -> new ReadOnlyStringWrapper(String.format("%,d", cd.getValue().getImportPrice().longValue())));
        colPriceUpdated
                .setCellValueFactory(cd -> new ReadOnlyStringWrapper(DateUtils.format(cd.getValue().getUpdatedAt())));
        loadPrices();
    }

    private void loadPrices() {
        try {
            String kw = searchPriceField.getText();
            List<com.example.storemanagement.model.SupplierProductPrice> list;
            if (kw == null || kw.isBlank()) {
                list = priceDAO.getAll();
            } else {
                list = priceDAO.search(kw);
            }
            priceTable.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            AlertUtils.error("Lỗi", "Không thể tải bảng giá: " + e.getMessage());
        }
    }

    @FXML
    private void onSearchPrice() {
        loadPrices();
    }

    @FXML
    private void onAddPrice() {
        Dialog<com.example.storemanagement.model.SupplierProductPrice> dialog = new Dialog<>();
        dialog.setTitle("Thêm giá nhập");
        dialog.setHeaderText("Nhập thông tin giá nhập hàng");

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        ComboBox<com.example.storemanagement.model.Product> productCombo = new ComboBox<>();
        ComboBox<com.example.storemanagement.model.Supplier> supplierCombo = new ComboBox<>();
        TextField priceField = new TextField();

        // Load data for combos
        try {
            productCombo.getItems().setAll(productDAO.search("", 1, 1000)); // Load top 1000 products
            supplierCombo.getItems().setAll(supplierDAO.getAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        grid.add(new Label("Sản phẩm:"), 0, 0);
        grid.add(productCombo, 1, 0);
        grid.add(new Label("Nhà cung cấp:"), 0, 1);
        grid.add(supplierCombo, 1, 1);
        grid.add(new Label("Giá nhập:"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    com.example.storemanagement.model.SupplierProductPrice p = new com.example.storemanagement.model.SupplierProductPrice();
                    p.setProductId(productCombo.getValue().getId());
                    p.setSupplierId(supplierCombo.getValue().getId());
                    p.setImportPrice(new java.math.BigDecimal(priceField.getText().trim()));
                    return p;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        java.util.Optional<com.example.storemanagement.model.SupplierProductPrice> result = dialog.showAndWait();

        result.ifPresent(p -> {
            try {
                priceDAO.upsert(p);
                loadPrices();
                AlertUtils.info("Thành công", "Đã lưu giá nhập hàng.");
            } catch (SQLException e) {
                AlertUtils.error("Lỗi", "Không thể lưu giá: " + e.getMessage());
            }
        });
    }

    @FXML
    private void onEditPrice() {
        com.example.storemanagement.model.SupplierProductPrice selected = priceTable.getSelectionModel()
                .getSelectedItem();
        if (selected == null) {
            AlertUtils.warn("Chưa chọn", "Vui lòng chọn dòng cần sửa.");
            return;
        }
        // Reuse Add logic but pre-fill (simplified for now, just show Add dialog as
        // Upsert handles update)
        // Ideally should pre-select combos and text field.
        // For brevity, I'll just call onAddPrice but let user re-select.
        // Better: Implement proper Edit dialog.

        Dialog<com.example.storemanagement.model.SupplierProductPrice> dialog = new Dialog<>();
        dialog.setTitle("Sửa giá nhập");
        dialog.setHeaderText("Cập nhật giá cho: " + selected.getProductName());

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField priceField = new TextField(String.valueOf(selected.getImportPrice().longValue()));

        grid.add(new Label("Sản phẩm:"), 0, 0);
        grid.add(new Label(selected.getProductName()), 1, 0);
        grid.add(new Label("Nhà cung cấp:"), 0, 1);
        grid.add(new Label(selected.getSupplierName()), 1, 1);
        grid.add(new Label("Giá nhập mới:"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    selected.setImportPrice(new java.math.BigDecimal(priceField.getText().trim()));
                    return selected;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        java.util.Optional<com.example.storemanagement.model.SupplierProductPrice> result = dialog.showAndWait();

        result.ifPresent(p -> {
            try {
                priceDAO.upsert(p);
                loadPrices();
                AlertUtils.info("Thành công", "Đã cập nhật giá.");
            } catch (SQLException e) {
                AlertUtils.error("Lỗi", "Không thể cập nhật: " + e.getMessage());
            }
        });
    }

    @FXML
    private void onDeletePrice() {
        com.example.storemanagement.model.SupplierProductPrice selected = priceTable.getSelectionModel()
                .getSelectedItem();
        if (selected == null) {
            AlertUtils.warn("Chưa chọn", "Vui lòng chọn dòng cần xóa.");
            return;
        }
        if (AlertUtils.confirm("Xác nhận", "Bạn có chắc muốn xóa giá nhập này?")) {
            try {
                priceDAO.delete(selected.getId());
                loadPrices();
                AlertUtils.info("Thành công", "Đã xóa giá nhập.");
            } catch (SQLException e) {
                AlertUtils.error("Lỗi", "Không thể xóa: " + e.getMessage());
            }
        }
    }
}
