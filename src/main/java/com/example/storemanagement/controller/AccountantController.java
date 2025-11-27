package com.example.storemanagement.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.example.storemanagement.dao.BudgetAllocationDAO;
import com.example.storemanagement.dao.BudgetExpenseDAO;
import com.example.storemanagement.dao.OrderEventDAO;
import com.example.storemanagement.dao.ProductDAO;
import com.example.storemanagement.dao.SupplierDebtDAO;
import com.example.storemanagement.model.Supplier;
import com.example.storemanagement.service.SupplierService;
import com.example.storemanagement.util.AlertUtils;
import com.example.storemanagement.util.Session;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controller for accountant main interface with 4 tabs.
 */
public class AccountantController {

    // Tab 1: Debt Management
    @FXML
    private ComboBox<SupplierItem> supplierCombo;
    @FXML
    private Label debtBalanceLabel;
    @FXML
    private TableView<DebtRow> debtTable;

    // Tab 2: Budget Allocation
    @FXML
    private ComboBox<StoreItem> storeCombo;
    @FXML
    private Label storeBalanceLabel;
    @FXML
    private TableView<AllocationRow> allocationTable;

    // Tab 3: Order Logs
    @FXML
    private ComboBox<StoreItem> orderLogStoreCombo;
    @FXML
    private DatePicker orderLogFromDate;
    @FXML
    private DatePicker orderLogToDate;
    @FXML
    private TableView<OrderLogRow> orderLogTable;

    // Tab 4: Inventory
    @FXML
    private ComboBox<StoreItem> inventoryStoreCombo;
    @FXML
    private TableView<InventoryRow> inventoryTable;

    @FXML
    private Label welcomeLabel;

    private SupplierDebtDAO debtDAO;
    private BudgetAllocationDAO allocationDAO;
    private BudgetExpenseDAO expenseDAO;
    private OrderEventDAO orderEventDAO;
    private ProductDAO productDAO;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Xin chào, " + Session.getUsername() + " (Accountant)");

        debtDAO = new SupplierDebtDAO();
        allocationDAO = new BudgetAllocationDAO();
        expenseDAO = new BudgetExpenseDAO();
        orderEventDAO = new OrderEventDAO();
        productDAO = new ProductDAO();

        setupTables();
        loadSuppliers();
        loadStores();
    }

    private void setupTables() {
        // Debt table
        TableColumn<DebtRow, String> debtDateCol = new TableColumn<>("Ngày");
        debtDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<DebtRow, String> debtTypeCol = new TableColumn<>("Loại giao dịch");
        debtTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<DebtRow, String> debtAmountCol = new TableColumn<>("Số tiền");
        debtAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<DebtRow, String> debtNoteCol = new TableColumn<>("Ghi chú");
        debtNoteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        TableColumn<DebtRow, String> debtUserCol = new TableColumn<>("Người tạo");
        debtUserCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        debtTable.getColumns().clear();
        debtTable.getColumns().addAll(debtDateCol, debtTypeCol, debtAmountCol, debtNoteCol, debtUserCol);

        // Allocation table
        TableColumn<AllocationRow, String> allocDateCol = new TableColumn<>("Ngày");
        allocDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<AllocationRow, String> allocStoreCol = new TableColumn<>("Cửa hàng");
        allocStoreCol.setCellValueFactory(new PropertyValueFactory<>("store"));
        TableColumn<AllocationRow, String> allocAmountCol = new TableColumn<>("Số tiền");
        allocAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<AllocationRow, String> allocNoteCol = new TableColumn<>("Ghi chú");
        allocNoteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        TableColumn<AllocationRow, String> allocUserCol = new TableColumn<>("Người cấp");
        allocUserCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        allocationTable.getColumns().clear();
        allocationTable.getColumns().addAll(allocDateCol, allocStoreCol, allocAmountCol, allocNoteCol, allocUserCol);

        // Order log table
        TableColumn<OrderLogRow, String> logTimeCol = new TableColumn<>("Thời gian");
        logTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<OrderLogRow, String> logOrderCol = new TableColumn<>("Mã đơn");
        logOrderCol.setCellValueFactory(new PropertyValueFactory<>("orderCode"));
        TableColumn<OrderLogRow, String> logEventCol = new TableColumn<>("Sự kiện");
        logEventCol.setCellValueFactory(new PropertyValueFactory<>("event"));
        TableColumn<OrderLogRow, String> logNoteCol = new TableColumn<>("Ghi chú");
        logNoteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        orderLogTable.getColumns().clear();
        orderLogTable.getColumns().addAll(logTimeCol, logOrderCol, logEventCol, logNoteCol);

        // Inventory table
        TableColumn<InventoryRow, String> invStoreCol = new TableColumn<>("Cửa hàng");
        invStoreCol.setCellValueFactory(new PropertyValueFactory<>("store"));
        TableColumn<InventoryRow, String> invSkuCol = new TableColumn<>("SKU");
        invSkuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        TableColumn<InventoryRow, String> invNameCol = new TableColumn<>("Tên sản phẩm");
        invNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        TableColumn<InventoryRow, String> invQtyCol = new TableColumn<>("Số lượng");
        invQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<InventoryRow, String> invUnitCol = new TableColumn<>("Đơn vị");
        invUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        inventoryTable.getColumns().clear();
        inventoryTable.getColumns().addAll(invStoreCol, invSkuCol, invNameCol, invQtyCol, invUnitCol);
    }

    private void loadSuppliers() {
        try {
            SupplierService supplierService = new SupplierService();
            List<Supplier> suppliers = supplierService.getAllSuppliers();
            List<SupplierItem> items = new ArrayList<>();
            for (Supplier s : suppliers) {
                items.add(new SupplierItem(s.getId(), s.getName()));
            }
            supplierCombo.setItems(FXCollections.observableArrayList(items));

            if (!items.isEmpty()) {
                supplierCombo.getSelectionModel().select(0);
                onSupplierSelected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStores() {
        // Load stores for all combo boxes
        try {
            // Simplified: hardcoded for demo or fetch from DB
            List<StoreItem> stores = List.of(
                    new StoreItem(1L, "HUST Mart – Cơ sở 1"),
                    new StoreItem(2L, "HUST Mart – Cơ sở 2"));

            storeCombo.setItems(FXCollections.observableArrayList(stores));
            orderLogStoreCombo.setItems(FXCollections.observableArrayList(stores));
            inventoryStoreCombo.setItems(FXCollections.observableArrayList(stores));

            if (!stores.isEmpty()) {
                storeCombo.getSelectionModel().select(0);
                orderLogStoreCombo.getSelectionModel().select(0);
                inventoryStoreCombo.getSelectionModel().select(0);
                onStoreSelected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSupplierSelected() {
        SupplierItem selected = supplierCombo.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        try {
            BigDecimal balance = debtDAO.getSupplierBalance(selected.id);
            debtBalanceLabel.setText(String.format("Tổng nợ: %,d VNĐ", balance.longValue()));

            // Load debt history
            var transactions = debtDAO.getTransactionHistory(selected.id, null, null);
            debtTable.getItems().clear();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (var t : transactions) {
                String typeDisplay = t.transactionType.equals("ADD_DEBT") ? "Thêm nợ"
                        : (t.transactionType.equals("PAY_DEBT") ? "Trả nợ" : "Điều chỉnh");
                debtTable.getItems().add(new DebtRow(
                        t.createdAt.format(dtf),
                        typeDisplay,
                        String.format("%,d", Math.abs(t.amount.longValue())),
                        t.note != null ? t.note : "",
                        t.createdBy != null ? t.createdBy : ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể tải dữ liệu công nợ: " + e.getMessage());
        }
    }

    @FXML
    private void onStoreSelected() {
        StoreItem selected = storeCombo.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        try {
            BigDecimal balance = expenseDAO.getCurrentBalance(selected.id);
            storeBalanceLabel.setText(String.format("Quỹ hiện tại: %,d VNĐ", balance.longValue()));

            // Load allocation history
            var allocations = allocationDAO.getHistory(selected.id, null, null);
            allocationTable.getItems().clear();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (var a : allocations) {
                allocationTable.getItems().add(new AllocationRow(
                        a.createdAt.format(dtf),
                        selected.name,
                        String.format("%,d", a.amount.longValue()),
                        a.note != null ? a.note : "",
                        a.createdBy != null ? a.createdBy : ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể tải dữ liệu quỹ: " + e.getMessage());
        }
    }

    @FXML
    private void onAddDebt() {
        SupplierItem selected = supplierCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Chưa chọn NCC", "Vui lòng chọn nhà cung cấp!");
            return;
        }

        TextInputDialog amountDialog = new TextInputDialog();
        amountDialog.setTitle("Thêm nợ");
        amountDialog.setHeaderText("Thêm nợ cho: " + selected.name);
        amountDialog.setContentText("Số tiền:");
        var amountResult = amountDialog.showAndWait();

        if (amountResult.isEmpty())
            return;

        TextInputDialog noteDialog = new TextInputDialog();
        noteDialog.setTitle("Ghi chú");
        noteDialog.setContentText("Ghi chú (tuỳ chọn):");
        var noteResult = noteDialog.showAndWait();

        try {
            BigDecimal amount = new BigDecimal(amountResult.get());
            String note = noteResult.orElse("");
            debtDAO.addDebt(selected.id, amount, note, Session.getUsername());
            AlertUtils.showInfo("Thành công", "Đã thêm nợ!");
            onSupplierSelected();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể thêm nợ: " + e.getMessage());
        }
    }

    @FXML
    private void onPayDebt() {
        SupplierItem selected = supplierCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Chưa chọn NCC", "Vui lòng chọn nhà cung cấp!");
            return;
        }

        TextInputDialog amountDialog = new TextInputDialog();
        amountDialog.setTitle("Trả nợ");
        amountDialog.setHeaderText("Trả nợ cho: " + selected.name);
        amountDialog.setContentText("Số tiền:");
        var amountResult = amountDialog.showAndWait();

        if (amountResult.isEmpty())
            return;

        TextInputDialog noteDialog = new TextInputDialog();
        noteDialog.setTitle("Ghi chú");
        noteDialog.setContentText("Ghi chú (tuỳ chọn):");
        var noteResult = noteDialog.showAndWait();

        try {
            BigDecimal amount = new BigDecimal(amountResult.get());
            String note = noteResult.orElse("");
            debtDAO.payDebt(selected.id, amount, note, Session.getUsername());
            AlertUtils.showInfo("Thành công", "Đã trả nợ!");
            onSupplierSelected();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể trả nợ: " + e.getMessage());
        }
    }

    @FXML
    private void onAllocateBudget() {
        StoreItem selected = storeCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Chưa chọn cửa hàng", "Vui lòng chọn cửa hàng!");
            return;
        }

        TextInputDialog amountDialog = new TextInputDialog();
        amountDialog.setTitle("Cấp quỹ");
        amountDialog.setHeaderText("Cấp quỹ cho: " + selected.name);
        amountDialog.setContentText("Số tiền:");
        var amountResult = amountDialog.showAndWait();

        if (amountResult.isEmpty())
            return;

        TextInputDialog noteDialog = new TextInputDialog();
        noteDialog.setTitle("Ghi chú");
        noteDialog.setContentText("Ghi chú (tuỳ chọn):");
        var noteResult = noteDialog.showAndWait();

        try {
            BigDecimal amount = new BigDecimal(amountResult.get());
            String note = noteResult.orElse("");
            allocationDAO.allocate(selected.id, amount, note, Session.getUsername());
            AlertUtils.showInfo("Thành công", "Đã cấp quỹ!");
            onStoreSelected();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể cấp quỹ: " + e.getMessage());
        }
    }

    @FXML
    private void onViewOrderLogs() {
        StoreItem selected = orderLogStoreCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Chưa chọn cửa hàng", "Vui lòng chọn cửa hàng!");
            return;
        }

        LocalDate fromDate = orderLogFromDate.getValue();
        LocalDate toDate = orderLogToDate.getValue();

        try {
            var events = orderEventDAO.getEventsByStore(selected.id, fromDate, toDate);
            orderLogTable.getItems().clear();

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (var event : events) {
                String orderCode = "ORD-" + event.getOrderId();
                orderLogTable.getItems().add(new OrderLogRow(
                        event.getCreatedAt().format(dtf),
                        orderCode,
                        event.getEventType(),
                        event.getNote() != null ? event.getNote() : ""));
            }

            String dateInfo = "";
            if (fromDate != null && toDate != null) {
                dateInfo = String.format(" từ %s đến %s", fromDate, toDate);
            } else if (fromDate != null) {
                dateInfo = String.format(" từ %s", fromDate);
            } else if (toDate != null) {
                dateInfo = String.format(" đến %s", toDate);
            }

            AlertUtils.showInfo("Thành công",
                    String.format("Đã tải %d log đơn hàng của %s%s", events.size(), selected.name, dateInfo));
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể tải log đơn hàng: " + e.getMessage());
        }
    }

    @FXML
    private void onViewInventory() {
        StoreItem selected = inventoryStoreCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Chưa chọn cửa hàng", "Vui lòng chọn cửa hàng!");
            return;
        }

        try {
            var items = productDAO.getInventoryOverview(selected.id, null, null, 1, 1000);
            inventoryTable.getItems().clear();

            for (var item : items) {
                inventoryTable.getItems().add(new InventoryRow(
                        item.storeName,
                        item.sku,
                        item.productName,
                        String.valueOf(item.quantity),
                        "Cái" // Default unit
                ));
            }

            AlertUtils.showInfo("Thành công", String.format("Đã tải %d sản phẩm của %s", items.size(), selected.name));
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể tải tồn kho: " + e.getMessage());
        }
    }

    @FXML
    private void onViewAllInventory() {
        try {
            // Get inventory for all stores (storeId = null)
            var items = productDAO.getInventoryOverview(null, null, null, 1, 1000);
            inventoryTable.getItems().clear();

            for (var item : items) {
                inventoryTable.getItems().add(new InventoryRow(
                        item.storeName,
                        item.sku,
                        item.productName,
                        String.valueOf(item.quantity),
                        "Cái" // Default unit
                ));
            }

            AlertUtils.showInfo("Thành công",
                    String.format("Đã tải tồn kho tổng hợp: %d sản phẩm từ tất cả cửa hàng", items.size()));
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể tải tồn kho: " + e.getMessage());
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.close();
    }

    // ===== Report & Audit Methods =====

    @FXML
    private void onReportInventorySystem() {
        AlertUtils.showInfo("Tính năng đang phát triển", "Báo cáo tồn kho toàn hệ thống");
    }

    @FXML
    private void onReportWarehouseLog() {
        AlertUtils.showInfo("Tính năng đang phát triển", "Báo cáo log biến động kho");
    }

    @FXML
    private void onReportSupplierDebt() {
        AlertUtils.showInfo("Tính năng đang phát triển", "Báo cáo công nợ nhà cung cấp");
    }

    @FXML
    private void onReportStoreFund() {
        AlertUtils.showInfo("Tính năng đang phát triển", "Báo cáo quỹ cửa hàng");
    }

    @FXML
    private void onReportImportPrice() {
        AlertUtils.showInfo("Tính năng đang phát triển", "Báo cáo giá nhập & lịch sử giá");
    }

    // ===== Inner Classes for ComboBox and TableView =====

    public static class SupplierItem {
        public final long id;
        public final String name;

        public SupplierItem(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class StoreItem {
        public final long id;
        public final String name;

        public StoreItem(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class DebtRow {
        private String date, type, amount, note, user;

        public DebtRow(String date, String type, String amount, String note, String user) {
            this.date = date;
            this.type = type;
            this.amount = amount;
            this.note = note;
            this.user = user;
        }

        public String getDate() {
            return date;
        }

        public String getType() {
            return type;
        }

        public String getAmount() {
            return amount;
        }

        public String getNote() {
            return note;
        }

        public String getUser() {
            return user;
        }
    }

    public static class AllocationRow {
        private String date, store, amount, note, user;

        public AllocationRow(String date, String store, String amount, String note, String user) {
            this.date = date;
            this.store = store;
            this.amount = amount;
            this.note = note;
            this.user = user;
        }

        public String getDate() {
            return date;
        }

        public String getStore() {
            return store;
        }

        public String getAmount() {
            return amount;
        }

        public String getNote() {
            return note;
        }

        public String getUser() {
            return user;
        }
    }

    public static class OrderLogRow {
        private String time, orderCode, event, note;

        public OrderLogRow(String time, String orderCode, String event, String note) {
            this.time = time;
            this.orderCode = orderCode;
            this.event = event;
            this.note = note;
        }

        public String getTime() {
            return time;
        }

        public String getOrderCode() {
            return orderCode;
        }

        public String getEvent() {
            return event;
        }

        public String getNote() {
            return note;
        }
    }

    public static class InventoryRow {
        private String store, sku, productName, quantity, unit;

        public InventoryRow(String store, String sku, String productName, String quantity, String unit) {
            this.store = store;
            this.sku = sku;
            this.productName = productName;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getStore() {
            return store;
        }

        public String getSku() {
            return sku;
        }

        public String getProductName() {
            return productName;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }
    }
}
