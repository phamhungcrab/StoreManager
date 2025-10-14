package com.example.storemanagement.controller;

/*
 * MainController.java – Controller cho main.fxml
 *
 * Nhiệm vụ chính:
 * 1) Điều hướng giữa các màn hình: Customers, Inventory, Finance.
 * 2) Nạp FXML con vào vùng contentArea (StackPane) ở giữa.
 * 3) Hiển thị trạng thái (DB URL, user) ở thanh status (statusLabel).
 * 4) Xử lý các action cơ bản: About, Exit.
 *
 * Phụ thuộc tối thiểu:
 * - Chỉ JavaFX + lớp Main (để đọc cấu hình DB). Không phụ thuộc DAO/Service
 *   nên có thể chạy ngay khi chưa tạo xong backend.
 *
 * Đường dẫn tài nguyên (resource path):
 * - "/fxml/customers.fxml"
 * - "/fxml/inventory.fxml"
 * - "/fxml/finance.fxml"
 *
 * Gợi ý sau này:
 * - Có thể tạo lớp NavigationService để tách việc nạp view.
 * - Có thể thêm Breadcrumb, hoặc TabPane nếu muốn nhiều tab.
 */

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import com.example.storemanagement.Main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {

    // Liên kết các node trong main.fxml qua fx:id
    @FXML private StackPane contentArea;   // vùng hiển thị màn hình con
    @FXML private Label statusLabel;       // hiển thị thông tin trạng thái (DB,...)

    /**
     * Hàm này tự động chạy sau khi FXMLLoader nạp xong FXML.
     * Dùng để khởi tạo giao diện mặc định (màn hình chào + status DB).
     */
    @FXML
    public void initialize() {
        // Cập nhật status DB (đọc từ Main.getDbProps())
        updateStatusBar();
        // Hiển thị màn hình chào mừng mặc định
        showWelcome();
    }

    /**
     * Mở màn hình Quản lý Khách hàng.
     */
    @FXML
    public void openCustomers(ActionEvent event) {
        loadContent("/fxml/customers.fxml", "Customers");
    }

    /**
     * Mở màn hình Quản lý Kho hàng.
     */
    @FXML
    public void openInventory(ActionEvent event) {
        loadContent("/fxml/inventory.fxml", "Inventory");
    }

    /**
     * Mở màn hình Quản lý Tài chính.
     */
    @FXML
    public void openFinance(ActionEvent event) {
        loadContent("/fxml/finance.fxml", "Finance");
    }

    /**
     * Hiển thị hộp thoại giới thiệu.
     */
    @FXML
    public void openAbout(ActionEvent event) {
        String content = "Store Management System\n" +
                "Phiên bản: 0.1 (Core, chưa có login)\n" +
                "Tác giả: Phạm Ngọc Hưng – MSSV: 20235342";
        showAlert(Alert.AlertType.INFORMATION, "About", content);
    }

    /**
     * Thoát ứng dụng một cách an toàn.
     */
    @FXML
    public void exitApp(ActionEvent event) {
        // Tắt toàn bộ JavaFX Application Thread
        javafx.application.Platform.exit();
    }

    // ================== Helpers ==================

    /**
     * Hiển thị một màn hình chào đơn giản khi khởi động.
     * Không phụ thuộc FXML con để tránh lỗi nếu chưa tạo customers.fxml, ...
     */
    private void showWelcome() {
        Label welcome = new Label("👋 Welcome!\nHãy dùng menu hoặc nút bên trái để chuyển màn hình.\n" +
                "Module hiện có: Customers, Inventory, Finance.\n" +
                "(Có thể thêm Login/Menu khác trong tương lai)");
        welcome.setWrapText(true);
        welcome.setStyle("-fx-font-size: 14px; -fx-padding: 24;");

        setContent(welcome);
    }

    /**
     * Tải FXML con và hiển thị vào contentArea. Nếu có lỗi, báo Alert dễ hiểu.
     * @param fxmlPath  đường dẫn trong classpath (ví dụ: /fxml/customers.fxml)
     * @param title     tên hiển thị (không bắt buộc, dùng cho thông điệp)
     */
    private void loadContent(String fxmlPath, String title) {
        try {
            URL fxml = getClass().getResource(fxmlPath);
            if (fxml == null) {
                throw new IllegalStateException("Không tìm thấy " + fxmlPath + " trong resources.");
            }
            Parent view = FXMLLoader.load(fxml);
            setContent(view);
            setStatusText("Opened: " + title);
        } catch (IOException | IllegalStateException ex) {
            showAlert(Alert.AlertType.ERROR, "Load View Failed",
                    "Không thể nạp màn hình: " + title + "\n" + ex.getMessage());
        }
    }

    /**
     * Thay thế nội dung ở contentArea bằng node mới.
     */
    private void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    /**
     * Cập nhật thanh trạng thái dựa trên cấu hình DB đang dùng.
     */
    private void updateStatusBar() {
        Properties p = Main.getDbProps();
        String url = p != null ? p.getProperty("url", "jdbc:mysql://localhost:3306/store_management") : "(unknown)";
        String user = p != null ? p.getProperty("user", "root") : "(unknown)";
        setStatusText("DB: " + url + "  |  User: " + user);
    }

    /**
     * Đặt nội dung text cho statusLabel (null-safe).
     */
    private void setStatusText(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    /** Hiển thị Alert tiện lợi */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.show();
    }
}
