package com.example.storemanagement;

/*
 * =============================================================
 *  Store Management System – Java + MySQL (no login for now)
 *  Main.java – Entry point for JavaFX, loads FXML, reads db.properties
 * =============================================================
 */

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class Main extends Application {
    private MediaPlayer mediaPlayer;
    public static final String APP_NAME = "Quản lý cửa hàng";
    private static Properties dbProps;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Play background music
        playBackgroundMusic("/audio/music_background.mp3");

        // Set window title
        primaryStage.setTitle(APP_NAME);

        // Load and set window icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/logostb.jpeg"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("⚠️ Không tìm thấy /images/logostb.jpeg: " + e.getMessage());
        }

        // Load DB properties (optional)
        dbProps = loadDbProperties();

        // Optional DB connection test (requires DBConnection class)
        try {
            var conn = com.example.storemanagement.dao.DBConnection.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                info("Kết nối MySQL thành công: " + dbProps.getProperty("url", "(chưa thiết lập)"));
            }
        } catch (Exception e) {
            warn("Không thể kết nối MySQL. Kiểm tra db.properties & MySQL service.\n" + e.getMessage());
        }

        // Load login UI and apply CSS
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("✅ Material Design CSS loaded for login scene");
            } else {
                System.err.println("⚠️ Không tìm thấy /css/style.css");
            }
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Không thể tải form đăng nhập: " + e.getMessage());
        }
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
            mediaPlayer.setVolume(0.9);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /** Load DB properties from classpath */
    private Properties loadDbProperties() {
        Properties props = new Properties();
        String path = "/database/db.properties";
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in != null) {
                props.load(in);
            } else {
                warn("Không tìm thấy " + path + ". Sẽ dùng giá trị mặc định.");
            }
        } catch (Exception e) {
            warn("Lỗi đọc " + path + ": " + e.getMessage());
        }
        return props;
    }

    // Helper alert methods
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

    // Utility to get resources
    private URL getResource(String path) {
        return getClass().getResource(path);
    }

    public static Properties getDbProps() {
        return dbProps;
    }
}