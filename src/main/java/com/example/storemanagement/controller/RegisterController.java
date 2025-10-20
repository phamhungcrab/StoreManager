package com.example.storemanagement.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.example.storemanagement.dao.DBConnection;
import com.example.storemanagement.util.PasswordUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText().trim();

        // Kiểm tra dữ liệu đầu vào
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Kiểm tra định dạng email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showAlert(AlertType.ERROR, "Định dạng email không hợp lệ!");
            return;
        }

        // Mã hóa mật khẩu
        String hashed = PasswordUtils.hashPassword(password);

        // Thực hiện ghi dữ liệu vào MySQL
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashed);
            ps.executeUpdate();

            showAlert(AlertType.INFORMATION, "Đăng ký thành công!");

            // Xóa dữ liệu nhập sau khi đăng ký xong
            usernameField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            
            // Chuyển về màn hình đăng nhập
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Tên đăng nhập hoặc email đã tồn tại!");
        }
    }

    @FXML
protected void onSwitchToLogin(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(scene);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
