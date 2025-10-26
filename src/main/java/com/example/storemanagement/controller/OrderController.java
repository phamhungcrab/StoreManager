package com.example.storemanagement.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.storemanagement.dao.DBConnection;
import com.example.storemanagement.service.OrderService;
import com.example.storemanagement.util.AlertUtils;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * OrderController – Màn hình tạo/chốt đơn đơn giản:
 * - Chọn Store, Customer, nhập Discount
 * - Thêm các dòng hàng (Product + Qty)
 * - Tính Subtotal/Grand total
 * - Confirm ➜ gọi OrderService.finalizeOrder(...)
 */
public class OrderController {

    // ==== UI refs ====
    @FXML
    private ComboBox<IdName> storeBox;
    @FXML
    private ComboBox<IdName> customerBox; // có thể null = khách vãng lai
    @FXML
    private TextField discountField;
    @FXML
    private Button addItemBtn, removeItemBtn, confirmBtn, refreshBtn;

    @FXML
    private TableView<OrderLine> itemsTable;
    @FXML
    private TableColumn<OrderLine, String> colSku;
    @FXML
    private TableColumn<OrderLine, String> colName;
    @FXML
    private TableColumn<OrderLine, Integer> colQty;
    @FXML
    private TableColumn<OrderLine, BigDecimal> colPrice;
    @FXML
    private TableColumn<OrderLine, BigDecimal> colTotal;

    @FXML
    private Label subtotalLbl, grandTotalLbl, statusLbl;

    // ==== Business ====
    private final OrderService orderService = new OrderService();
    private final ObservableList<OrderLine> lines = FXCollections.observableArrayList();

    // cache products: id -> (sku, name, price)
    private final Map<Long, ProductLite> products = new HashMap<>();

    @FXML
    public void initialize() {
        // load stores & customers & products
        storeBox.setItems(FXCollections.observableArrayList(loadStores()));
        customerBox.setItems(FXCollections.observableArrayList(loadCustomers()));
        loadProductsIntoCache();
        if (!storeBox.getItems().isEmpty())
            storeBox.getSelectionModel().selectFirst();

        // table
        itemsTable.setItems(lines);
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        addItemBtn.setOnAction(e -> onAddItem());
        removeItemBtn.setOnAction(e -> onRemoveItem());
        refreshBtn.setOnAction(e -> recalcTotals());
        confirmBtn.setOnAction(e -> onConfirm());

        recalcTotals();
    }

    private void onAddItem() {
        // Dialog chọn sản phẩm + số lượng
        ChoiceDialog<IdName> dlg = new ChoiceDialog<>();
        dlg.setTitle("Thêm sản phẩm");
        dlg.setHeaderText("Chọn sản phẩm");
        List<IdName> opts = new ArrayList<>();
        for (ProductLite p : products.values())
            opts.add(new IdName(p.id, p.sku + " – " + p.name));
        opts.sort(Comparator.comparing(o -> o.name));
        dlg.getItems().setAll(opts);
        if (!opts.isEmpty())
            dlg.setSelectedItem(opts.get(0)); // ✅ SỬA Ở ĐÂY

        Optional<IdName> chosen = dlg.showAndWait();
        if (chosen.isEmpty())
            return;
        ProductLite pl = products.get(chosen.get().id);

        TextInputDialog qtyDlg = new TextInputDialog("1");
        qtyDlg.setTitle("Số lượng");
        qtyDlg.setHeaderText(pl.sku + " – " + pl.name);
        qtyDlg.setContentText("Nhập số lượng:");
        Optional<String> qtyStr = qtyDlg.showAndWait();
        if (qtyStr.isEmpty())
            return;
        try {
            int qty = Integer.parseInt(qtyStr.get().trim());
            if (qty <= 0)
                throw new NumberFormatException();
            lines.add(new OrderLine(pl.id, pl.sku, pl.name, qty, pl.price));
            recalcTotals();
        } catch (NumberFormatException nfe) {
            AlertUtils.warn("Giá trị không hợp lệ", "Số lượng phải là số nguyên dương");
        }
    }

    private void onRemoveItem() {
        OrderLine sel = itemsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtils.warn("No selection", "Chọn 1 dòng để xoá");
            return;
        }
        lines.remove(sel);
        recalcTotals();
    }

    private void onConfirm() {
        if (storeBox.getValue() == null) {
            AlertUtils.warn("Thiếu dữ liệu", "Chọn cửa hàng");
            return;
        }
        if (lines.isEmpty()) {
            AlertUtils.warn("Giỏ hàng trống", "Hãy thêm ít nhất 1 sản phẩm");
            return;
        }
        Long storeId = storeBox.getValue().id;
        Long customerId = customerBox.getValue() == null ? null : customerBox.getValue().id;
        BigDecimal discount = parseMoney(discountField.getText());
        if (discount.signum() < 0) {
            AlertUtils.warn("Discount không hợp lệ", "Không được âm");
            return;
        }

        List<OrderService.OrderItemInput> items = new ArrayList<>();
        for (OrderLine l : lines)
            items.add(new OrderService.OrderItemInput(l.productId, l.quantity.get()));

        try {
            long orderId = orderService.finalizeOrder(storeId, customerId, items, discount, null, "CASH");
            AlertUtils.info("Thành công", "Đã tạo đơn hàng #" + orderId);
            lines.clear();
            discountField.setText("0");
            recalcTotals();
        } catch (Exception ex) {
            AlertUtils.error("Finalize failed", ex.getMessage());
        }
    }

    private void recalcTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLine l : lines)
            subtotal = subtotal.add(l.getLineTotal());
        BigDecimal discount = parseMoney(discountField.getText());
        BigDecimal grand = subtotal.subtract(discount);
        if (grand.signum() < 0)
            grand = BigDecimal.ZERO;
        subtotalLbl.setText("Subtotal: " + subtotal.toPlainString());
        grandTotalLbl.setText("Grand Total: " + grand.toPlainString());
        statusLbl.setText(lines.size() + " item(s)");
    }

    // ================= DB loads =================
    private List<IdName> loadStores() {
        List<IdName> list = new ArrayList<>();
        String sql = "SELECT id, name FROM stores ORDER BY name";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new IdName(rs.getLong(1), rs.getString(2)));
        } catch (SQLException ignored) {
        }
        return list;
    }

    private List<IdName> loadCustomers() {
        List<IdName> list = new ArrayList<>();
        String sql = "SELECT id, name FROM customers ORDER BY name";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new IdName(rs.getLong(1), rs.getString(2)));
        } catch (SQLException ignored) {
        }
        return list;
    }

    private void loadProductsIntoCache() {
        String sql = "SELECT id, sku, name, unit_price FROM products WHERE active=1 ORDER BY name";
        try (Connection cn = DBConnection.getInstance().getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                products.put(id,
                        new ProductLite(id, rs.getString("sku"), rs.getString("name"), rs.getBigDecimal("unit_price")));
            }
        } catch (SQLException ignored) {
        }
    }

    private BigDecimal parseMoney(String s) {
        try {
            return new BigDecimal(s == null || s.isBlank() ? "0" : s.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // ================= Table Line model =================
    public static class OrderLine {
        private final long productId;
        private final StringProperty sku = new SimpleStringProperty();
        private final StringProperty name = new SimpleStringProperty();
        private final IntegerProperty quantity = new SimpleIntegerProperty(1);
        private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>(BigDecimal.ZERO);
        private final ObjectProperty<BigDecimal> lineTotal = new SimpleObjectProperty<>(BigDecimal.ZERO);

        public OrderLine(long productId, String sku, String name, int qty, BigDecimal price) {
            this.productId = productId;
            this.sku.set(sku);
            this.name.set(name);
            this.quantity.set(qty);
            this.unitPrice.set(price);
            recalc();
            this.quantity.addListener((o, ov, nv) -> recalc());
        }

        private void recalc() {
            BigDecimal q = new BigDecimal(quantity.get());
            this.lineTotal.set(unitPrice.get().multiply(q));
        }

        public long getProductId() {
            return productId;
        }

        public String getSku() {
            return sku.get();
        }

        public StringProperty skuProperty() {
            return sku;
        }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public int getQuantity() {
            return quantity.get();
        }

        public IntegerProperty quantityProperty() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice.get();
        }

        public ObjectProperty<BigDecimal> unitPriceProperty() {
            return unitPrice;
        }

        public BigDecimal getLineTotal() {
            return lineTotal.get();
        }

        public ObjectProperty<BigDecimal> lineTotalProperty() {
            return lineTotal;
        }
    }

    // ================= Small helpers =================
    public static class IdName {
        public final long id;
        public final String name;

        public IdName(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class ProductLite {
        long id;
        String sku;
        String name;
        BigDecimal price;

        ProductLite(long i, String s, String n, BigDecimal p) {
            id = i;
            sku = s;
            name = n;
            price = p;
        }
    }
}