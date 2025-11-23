package com.example.storemanagement.controller;

import com.example.storemanagement.dao.UserDAO;
import com.example.storemanagement.model.User;
import com.example.storemanagement.util.AlertUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class UserController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colRole;

    @FXML
    private Label lblSelectedUser;
    @FXML
    private ComboBox<String> cbRole;

    private final UserDAO userDAO = new UserDAO();
    private User selectedUser;

    @FXML
    public void initialize() {
        // Init columns
        colUsername.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getUsername()));
        colEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmail()));
        colRole.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getRole()));

        // Init Role ComboBox
        cbRole.setItems(FXCollections.observableArrayList("admin", "user", "seller", "logistic", "accountant"));

        // Table selection listener
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selectedUser = val;
            if (val != null) {
                lblSelectedUser.setText(val.getUsername());
                cbRole.setValue(val.getRole());
            } else {
                lblSelectedUser.setText("");
                cbRole.setValue(null);
            }
        });

        loadData();
    }

    private void loadData() {
        try {
            List<User> list = userDAO.getAllUsers();
            userTable.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            AlertUtils.error("Error", "Failed to load users: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdateRole() {
        if (selectedUser == null) {
            AlertUtils.warn("No selection", "Please select a user first.");
            return;
        }
        String newRole = cbRole.getValue();
        if (newRole == null) {
            AlertUtils.warn("No role", "Please select a role.");
            return;
        }

        try {
            if (userDAO.updateUserRole(selectedUser.getUsername(), newRole)) {
                AlertUtils.info("Success", "Updated role for " + selectedUser.getUsername() + " to " + newRole);
                loadData(); // Refresh table
            } else {
                AlertUtils.error("Failed", "Could not update user role.");
            }
        } catch (SQLException e) {
            AlertUtils.error("Error", "Database error: " + e.getMessage());
        }
    }
}
