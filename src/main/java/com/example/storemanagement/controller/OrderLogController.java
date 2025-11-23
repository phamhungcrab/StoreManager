package com.example.storemanagement.controller;

import com.example.storemanagement.dao.OrderEventDAO;
import com.example.storemanagement.model.OrderEvent;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;

public class OrderLogController {

    @FXML
    private TableView<OrderEvent> logTable;
    @FXML
    private TableColumn<OrderEvent, Long> colId;
    @FXML
    private TableColumn<OrderEvent, Long> colOrderId;
    @FXML
    private TableColumn<OrderEvent, String> colType;
    @FXML
    private TableColumn<OrderEvent, String> colNote;
    @FXML
    private TableColumn<OrderEvent, String> colTime;

    // New columns
    @FXML
    private TableColumn<OrderEvent, String> colCustomer;
    @FXML
    private TableColumn<OrderEvent, String> colProducts;
    @FXML
    private TableColumn<OrderEvent, java.math.BigDecimal> colTotal;

    private final OrderEventDAO dao = new OrderEventDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colType.setCellValueFactory(new PropertyValueFactory<>("eventType"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Bind new columns
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colProducts.setCellValueFactory(new PropertyValueFactory<>("productDetails"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Format LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        colTime.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(formatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        loadData();
    }

    private void loadData() {
        logTable.setItems(FXCollections.observableArrayList(dao.getAllEvents()));
    }
}
