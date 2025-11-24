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
}
