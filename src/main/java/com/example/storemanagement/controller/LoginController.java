package com.example.storemanagement.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.example.storemanagement.dao.DBConnection;
import com.example.storemanagement.util.PasswordUtils;
import com.example.storemanagement.util.Session;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String input = usernameField.getText().trim();
        String password = passwordField.getText();

        if (input.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Vui lòng nhập đủ thông tin!");
            return;
        }

        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, input);
            ps.setString(2, input);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashed = rs.getString("password");
                if (PasswordUtils.checkPassword(password, hashed)) {
                    showAlert(AlertType.INFORMATION, "Đăng nhập thành công!");
                    String role = rs.getString("role");
                    Session.setUser(rs.getString("username"), role);

                    openMainView();
                } else {
                    showAlert(AlertType.ERROR, "Sai mật khẩu!");
                }
            } else {
                showAlert(AlertType.ERROR, "Không tìm thấy tài khoản!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi kết nối hoặc truy vấn CSDL!");
        }
    }

    @FXML
    protected void onSwitchToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Không thể mở giao diện chính!");
        }
    }

    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
