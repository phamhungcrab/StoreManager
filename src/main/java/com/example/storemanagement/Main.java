package com.example.storemanagement;

/*
 * =============================================================
 *  Store Management System – Java + MySQL (no login for now)
 *  Main.java – Điểm khởi chạy JavaFX, nạp FXML chính, đọc db.properties
 *
 *  Thư mục liên quan (theo README):
 *  src/main/resources/
 *    ├─ fxml/main.fxml            ← Giao diện khung chính (controller: MainController)
 *    ├─ fxml/customers.fxml       ← Màn hình Khách hàng
 *    ├─ fxml/inventory.fxml       ← Màn hình Kho hàng
 *    ├─ fxml/finance.fxml         ← Màn hình Tài chính
 *    ├─ css/style.css             ← CSS cho UI (tùy chọn)
 *    └─ database/db.properties    ← Thông tin kết nối MySQL
 *
 *  Cách chạy (Maven):
 *  mvn clean compile
 *  mvn exec:java -Dexec.mainClass="com.example.storemanagement.Main"
 *
 *  Nếu dùng JavaFX SDK độc lập (không dùng BOM), VM Options cần:
 *    --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml
 *  (Trên macOS/Linux dùng $PATH_TO_FX)
 *
 *  Gợi ý kiến trúc:
 *  - Controller: com.example.storemanagement.controller.*
 *  - Service:    com.example.storemanagement.service.*
 *  - DAO:        com.example.storemanagement.dao.* (JDBC → MySQL)
 *  - Model:      com.example.storemanagement.model.*
 *  - Util:       com.example.storemanagement.util.*
 *
 *  Ghi chú:
 *  - File này **không** gọi trực tiếp DAO/Service để đảm bảo compile chạy được
 *    ngay cả khi bạn chưa tạo xong các lớp còn lại. Phần "kiểm tra kết nối DB"
 *    có ví dụ nhưng được COMMENT lại. Bạn mở comment khi đã tạo DBConnection.
 * =============================================================
 */

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    // Tên app hiển thị trên cửa sổ
    public static final String APP_NAME = "Store Management System";

    // Thuộc tính CSDL đọc từ resources/database/db.properties
    private static Properties dbProps;

    public static void main(String[] args) {
        // Điểm vào chuẩn JavaFX – sẽ gọi start(Stage)
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 1) Đặt tiêu đề cửa sổ
        primaryStage.setTitle(APP_NAME);

        // 2) Đọc cấu hình DB (nếu có). Không bắt buộc để chạy UI.
        dbProps = loadDbProperties();

        // 3) Cố gắng nạp giao diện chính từ FXML
        try {
            Scene scene = loadMainScene();
            attachCssIfPresent(scene, "/css/style.css");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception ex) {
            // Nếu FXML hoặc controller chưa sẵn sàng → dựng giao diện dự phòng
            Scene fallback = buildFallbackScene(ex);
            primaryStage.setScene(fallback);
            primaryStage.show();
        }

        // 4) (TÙY CHỌN) Kiểm tra kết nối DB khi bạn đã có DBConnection
        //    → BỎ COMMENT sau khi tạo lớp dao.DBConnection và MySQL Connector
        // try {
        //     var conn = com.example.storemanagement.dao.DBConnection.getInstance().getConnection();
        //     if (conn != null && !conn.isClosed()) {
        //         info("Kết nối MySQL thành công: " + dbProps.getProperty("url", "(chưa thiết lập)") );
        //     }
        // } catch (Exception e) {
        //     warn("Không thể kết nối MySQL. Kiểm tra db.properties & MySQL service.\n" + e.getMessage());
        // }
    }

    /**
     * Nạp Scene từ main.fxml (nằm trong resources/fxml/main.fxml)
     */
    private Scene loadMainScene() throws Exception {
        URL fxml = getResource("/fxml/main.fxml");
        if (fxml == null) {
            throw new IllegalStateException("Không tìm thấy /fxml/main.fxml trong resources.");
        }
        Parent root = FXMLLoader.load(fxml);
        Scene scene = new Scene(root);
        return scene;
    }

    /**
     * Đọc file cấu hình DB từ classpath: /database/db.properties
     * Trả về Properties rỗng nếu không tìm thấy để không chặn việc chạy UI.
     */
    private Properties loadDbProperties() {
        Properties props = new Properties();
        String path = "/database/db.properties";
        try (InputStream in = getResourceAsStream(path)) {
            if (in != null) {
                props.load(in);
            } else {
                warn("Không tìm thấy " + path + ". Sẽ dùng giá trị mặc định.");
            }
        } catch (Exception e) {
            warn("Lỗi đọc " + path + ": " + e.getMessage());
        }

        // Giá trị mặc định an toàn (không kết nối thật)
        props.putIfAbsent("url", "jdbc:mysql://localhost:3306/store_management");
        props.putIfAbsent("user", "root");
        props.putIfAbsent("password", "aaaa");
        return props;
    }

    /**
     * Gắn CSS nếu file tồn tại trong classpath.
     */
    private void attachCssIfPresent(Scene scene, String cssClasspathPath) {
        URL css = getResource(cssClasspathPath);
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

    /**
     * UI dự phòng khi chưa có FXML/Controller – giúp bạn chạy thử ngay.
     * Hiển thị thông báo cần tạo fxml & controller theo đúng cấu trúc.
     */
    private Scene buildFallbackScene(Exception cause) {
        BorderPane root = new BorderPane();
        String message = "👋 Xin chào, dự án đã chạy!\n\n" +
                "Chưa nạp được main.fxml. Hãy tạo: src/main/resources/fxml/main.fxml\n" +
                "Controller gợi ý: com.example.storemanagement.controller.MainController\n\n" +
                "Chi tiết lỗi: " + (cause != null ? cause.getMessage() : "(không rõ)");
        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-padding: 24; -fx-font-size: 14px;");
        root.setCenter(label);
        return new Scene(root, 960, 600);
    }

    // ===== Helpers nhỏ cho Alert =====
    private void info(String msg) {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", msg);
    }

    private void warn(String msg) {
        showAlert(Alert.AlertType.WARNING, "Chú ý", msg);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    // ===== Truy xuất tài nguyên tiện lợi =====
    private URL getResource(String path) {
        return getClass().getResource(path);
    }

    private InputStream getResourceAsStream(String path) {
        return getClass().getResourceAsStream(path);
    }

    /**
     * Cho phép module khác đọc cấu hình DB đã load (nếu cần).
     * Ví dụ trong DAO bạn có thể dùng Main.getDbProps().getProperty("url").
     */
    public static Properties getDbProps() {
        return dbProps;
    }
}
