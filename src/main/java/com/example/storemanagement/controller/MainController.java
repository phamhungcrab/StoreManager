package com.example.storemanagement.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import com.example.storemanagement.Main;
import com.example.storemanagement.model.User;
import com.example.storemanagement.util.Session;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Label statusLabel;
    @FXML
    private MenuItem toggleMusicItem;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button logoutBtn;

    @FXML
    private Button btnCustomers;
    @FXML
    private Button btnInventory;
    @FXML
    private Button btnFinance;
    @FXML
    private Button btnOrders;
    @FXML
    private Button btnOrderLogs;
    @FXML
    private Button btnLogistics;
    @FXML
    private Button btnUsers;

    private MediaPlayer mediaPlayer;
    private boolean musicPlaying = false;

    @FXML
    public void initialize() {
        playBackgroundMusic("/audio/music_background.mp3");
        updateStatusBar();
        showWelcome();

        String username = Session.getUsername();
        String role = Session.getRole();
        if (username != null) {
            welcomeLabel.setText("Xin chào, " + username + " (" + role + ")!");
        } else {
            welcomeLabel.setText("Xin chào, khách!");
        }

        setupPermissions(role);
    }

    private void setupPermissions(String role) {
        if (role == null)
            role = "user";

        boolean isLogistic = "logistic".equalsIgnoreCase(role);
        boolean isSeller = "seller".equalsIgnoreCase(role);
        boolean isAccountant = "accountant".equalsIgnoreCase(role);
        boolean isAdmin = "admin".equalsIgnoreCase(role);

        if (btnLogistics != null) {
            btnLogistics.setVisible(isAdmin || isLogistic);
            btnLogistics.setManaged(isAdmin || isLogistic);
        }

        if (btnFinance != null) {
            btnFinance.setVisible(isAdmin || isAccountant);
            btnFinance.setManaged(isAdmin || isAccountant);
        }

        if (btnCustomers != null) {
            btnCustomers.setVisible(isAdmin || isSeller);
            btnCustomers.setManaged(isAdmin || isSeller);
        }
        if (btnOrders != null) {
            btnOrders.setVisible(isAdmin || isSeller);
            btnOrders.setManaged(isAdmin || isSeller);
        }

        if (btnInventory != null) {
            btnInventory.setVisible(isAdmin || isSeller || isLogistic);
            btnInventory.setManaged(isAdmin || isSeller || isLogistic);
        }

        if (btnOrderLogs != null) {
            btnOrderLogs.setVisible(isAdmin || isSeller || isAccountant);
            btnOrderLogs.setManaged(isAdmin || isSeller || isAccountant);
        }

        if (btnUsers != null) {
            btnUsers.setVisible(isAdmin);
            btnUsers.setManaged(isAdmin);
        }
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        stopMusic();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Đăng nhập");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openCustomers(ActionEvent event) {
        loadContent("/fxml/customers.fxml", "Customers");
    }

    @FXML
    public void openInventory(ActionEvent event) {
        loadContent("/fxml/inventory.fxml", "Inventory");
    }

    @FXML
    public void openFinance(ActionEvent event) {
        loadContent("/fxml/finance.fxml", "Finance");
    }

    @FXML
    public void openOrders(ActionEvent event) {
        loadContent("/fxml/orders.fxml", "Orders");
    }

    @FXML
    public void openOrderLogs(ActionEvent event) {
        loadContent("/fxml/order_logs.fxml", "Order Logs");
    }

    @FXML
    public void openLogistics(ActionEvent event) {
        loadContent("/fxml/logistics.fxml", "Logistics Dashboard");
    }

    @FXML
    public void openUsers(ActionEvent event) {
        loadContent("/fxml/users.fxml", "User Management");
    }

    @FXML
    public void openAbout(ActionEvent event) {
        String content = "Store Management System\n" +
                "Phiên bản: 0.1 (Core, chưa có login)\n" +
                "Tác giả: Phạm Ngọc Hưng – MSSV: 20235342";
        showAlert(Alert.AlertType.INFORMATION, "About", content);
    }

    @FXML
    public void exitApp(ActionEvent event) {
        javafx.application.Platform.exit();
    }

    private void showWelcome() {
        Label welcome = new Label("👋 Welcome!\nHãy dùng menu hoặc nút bên trái để chuyển màn hình.\n" +
                "Module hiện có: Customers, Inventory, Finance.\n" +
                "(Có thể thêm Login/Menu khác trong tương lai)");
        welcome.setWrapText(true);
        welcome.setStyle("-fx-font-size: 14px; -fx-padding: 24;");
        setContent(welcome);
    }

    private void loadContent(String fxmlPath, String title) {
        try {
            URL fxml = getClass().getResource(fxmlPath);
            if (fxml == null) {
                throw new IllegalStateException("Không tìm thấy " + fxmlPath + " trong resources.");
            }
            Parent view = FXMLLoader.load(fxml);
            System.out.println("✅ Loaded view: " + title);
            setContent(view);
            setStatusText("Opened: " + title);
        } catch (IOException | IllegalStateException ex) {
            System.err.println("⚠️ Lỗi khi load " + fxmlPath);
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load View Failed",
                    "Không thể nạp màn hình: " + title + "\n" + ex.getMessage());
        }
    }

    private void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    private void updateStatusBar() {
        Properties p = Main.getDbProps();
        String url = p != null ? p.getProperty("url", "jdbc:mysql://localhost:3306/store_management") : "(unknown)";
        String user = p != null ? p.getProperty("user", "root") : "(unknown)";
        setStatusText("DB: " + url + "  |  User: " + user);
    }

    private void setStatusText(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.show();
    }

    private void playBackgroundMusic(String resourcePath) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                System.err.println("Không tìm thấy file nhạc: " + resourcePath);
                return;
            }

            Media media = new Media(resource.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(0.25);
            mediaPlayer.play();
            musicPlaying = true;

            if (toggleMusicItem != null)
                toggleMusicItem.setText("🔊 Tắt nhạc nền");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onToggleMusic() {
        if (mediaPlayer == null)
            return;
        if (musicPlaying) {
            mediaPlayer.pause();
            toggleMusicItem.setText("🔈 Bật nhạc nền");
        } else {
            mediaPlayer.play();
            toggleMusicItem.setText("🔊 Tắt nhạc nền");
        }
        musicPlaying = !musicPlaying;
    }

    public void stopMusic() {
        if (mediaPlayer != null)
            mediaPlayer.stop();
    }
}
