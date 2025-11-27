package com.example.storemanagement.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.example.storemanagement.dao.BudgetExpenseDAO;
import com.example.storemanagement.util.AlertUtils;
import com.example.storemanagement.util.Session;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controller for seller budget spending interface.
 */
public class SellerBudgetController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label balanceLabel;

    @FXML
    private TextField amountField;
    @FXML
    private TextField reasonField;
    @FXML
    private TextArea noteArea;

    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private TableView<ExpenseRow> expenseTable;

    private BudgetExpenseDAO expenseDAO;
    private Long storeId;

    @FXML
    public void initialize() {
        expenseDAO = new BudgetExpenseDAO();
        storeId = Session.getStoreId();

        if (storeId == null) {
            AlertUtils.showError("Lỗi", "Bạn chưa được gán cửa hàng. Liên hệ Admin.");
            return;
        }

        welcomeLabel.setText("Xin chào, " + Session.getUsername() + " (Seller)");
        setupTable();
        loadCurrentBalance();
        loadExpenseHistory();
    }

    private void setupTable() {
        TableColumn<ExpenseRow, String> dateCol = new TableColumn<>("Ngày giờ");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        TableColumn<ExpenseRow, String> amountCol = new TableColumn<>("Số tiền");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<ExpenseRow, String> reasonCol = new TableColumn<>("Lý do");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));

        TableColumn<ExpenseRow, String> noteCol = new TableColumn<>("Ghi chú");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        TableColumn<ExpenseRow, String> userCol = new TableColumn<>("Người chi");
        userCol.setCellValueFactory(new PropertyValueFactory<>("createdBy"));

        expenseTable.getColumns().clear();
        expenseTable.getColumns().addAll(dateCol, amountCol, reasonCol, noteCol, userCol);
    }

    private void loadCurrentBalance() {
        try {
            BigDecimal balance = expenseDAO.getCurrentBalance(storeId);
            balanceLabel.setText(String.format("Quỹ hiện tại: %,d VNĐ", balance.longValue()));
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể tải số dư quỹ: " + e.getMessage());
        }
    }

    private void loadExpenseHistory() {
        try {
            LocalDate from = fromDatePicker.getValue();
            LocalDate to = toDatePicker.getValue();

            var expenses = expenseDAO.getHistory(storeId, from, to);
            expenseTable.getItems().clear();

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (var exp : expenses) {
                expenseTable.getItems().add(new ExpenseRow(
                        exp.createdAt.format(dtf),
                        String.format("%,d", exp.amount.longValue()),
                        exp.reason,
                        exp.note != null ? exp.note : "",
                        exp.createdBy != null ? exp.createdBy : ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể tải lịch sử: " + e.getMessage());
        }
    }

    @FXML
    private void onRecordExpense() {
        try {
            // Validate input
            String amountStr = amountField.getText().trim();
            String reason = reasonField.getText().trim();
            String note = noteArea.getText().trim();

            if (amountStr.isEmpty() || reason.isEmpty()) {
                AlertUtils.showWarning("Thiếu thông tin", "Vui lòng nhập đầy đủ số tiền và lý do!");
                return;
            }

            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                AlertUtils.showWarning("Số tiền không hợp lệ", "Số tiền phải lớn hơn 0!");
                return;
            }

            // Record expense
            expenseDAO.recordExpense(storeId, amount, reason, note, Session.getUsername());

            AlertUtils.showInfo("Thành công", "Đã ghi nhận chi tiêu!");

            // Refresh
            onClear();
            loadCurrentBalance();
            loadExpenseHistory();

        } catch (NumberFormatException e) {
            AlertUtils.showError("Lỗi", "Số tiền không hợp lệ!");
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage();
            if (msg != null && msg.contains("Insufficient budget")) {
                AlertUtils.showError("Quỹ không đủ", "Số dư quỹ không đủ để chi tiêu số tiền này!");
            } else {
                AlertUtils.showError("Lỗi", "Không thể ghi nhận chi tiêu: " + msg);
            }
        }
    }

    @FXML
    private void onClear() {
        amountField.clear();
        reasonField.clear();
        noteArea.clear();
    }

    @FXML
    private void onFilter() {
        loadExpenseHistory();
    }

    @FXML
    private void onReportIncomeExpense() {
        AlertUtils.showInfo("Tính năng đang phát triển", "Chức năng Báo cáo thu chi đang được xây dựng.");
    }

    @FXML
    private void onSendReportToAccountant() {
        AlertUtils.showInfo("Tính năng đang phát triển", "Chức năng Gửi báo cáo cho kế toán đang được xây dựng.");
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) amountField.getScene().getWindow();
        stage.close();
    }

    // ===== Inner Class for TableView =====
    public static class ExpenseRow {
        private String createdAt;
        private String amount;
        private String reason;
        private String note;
        private String createdBy;

        public ExpenseRow(String createdAt, String amount, String reason, String note, String createdBy) {
            this.createdAt = createdAt;
            this.amount = amount;
            this.reason = reason;
            this.note = note;
            this.createdBy = createdBy;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getAmount() {
            return amount;
        }

        public String getReason() {
            return reason;
        }

        public String getNote() {
            return note;
        }

        public String getCreatedBy() {
            return createdBy;
        }
    }
}
