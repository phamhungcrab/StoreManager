package com.example.storemanagement.controller;

import com.example.storemanagement.dao.StoreDAO;
import com.example.storemanagement.dao.UserDAO;
import com.example.storemanagement.model.Store;
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
    @FXML
    private TableColumn<User, String> colStore;
    @FXML
    private ComboBox<Store> cbStore;

    private final UserDAO userDAO = new UserDAO();
    private final StoreDAO storeDAO = new StoreDAO();
    private User selectedUser;

    @FXML
    public void initialize() {
        // Init columns
        colUsername.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getUsername()));
        colEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmail()));
        colRole.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getRole()));
        colStore.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getStoreName()));

        // Init Role ComboBox
        cbRole.setItems(FXCollections.observableArrayList("admin", "user", "seller", "logistic", "accountant"));

        // Init Store ComboBox
        try {
            List<Store> stores = storeDAO.getAllStores();
            cbStore.setItems(FXCollections.observableArrayList(stores));
        } catch (SQLException e) {
            AlertUtils.error("Error", "Failed to load stores: " + e.getMessage());
        }

        // Table selection listener
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selectedUser = val;
            if (val != null) {
                lblSelectedUser.setText(val.getUsername());
                cbRole.setValue(val.getRole());
                // Select current store in ComboBox
                if (val.getStoreId() != null) {
                    for (Store s : cbStore.getItems()) {
                        if (s.getId().equals(val.getStoreId())) {
                            cbStore.setValue(s);
                            break;
                        }
                    }
                } else {
                    cbStore.setValue(null);
                }
            } else {
                lblSelectedUser.setText("");
                cbRole.setValue(null);
                cbStore.setValue(null);
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

    @FXML
    private void onAssignStore() {
        if (selectedUser == null) {
            AlertUtils.warn("No selection", "Please select a user first.");
            return;
        }
        Store selectedStore = cbStore.getValue();
        Long storeId = (selectedStore != null) ? selectedStore.getId() : null;

        try {
            if (userDAO.assignStore(selectedUser.getUsername(), storeId)) {
                String storeName = (selectedStore != null) ? selectedStore.getName() : "None";
                AlertUtils.info("Success", "Assigned store " + storeName + " to " + selectedUser.getUsername());
                loadData(); // Refresh table
            } else {
                AlertUtils.error("Failed", "Could not assign store.");
            }
        } catch (SQLException e) {
            AlertUtils.error("Error", "Database error: " + e.getMessage());
        }
    }
}
