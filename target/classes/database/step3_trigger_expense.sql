USE store_management;

DELIMITER $$

DROP TRIGGER IF EXISTS trg_expense_after_insert $$

CREATE TRIGGER trg_expense_after_insert
AFTER INSERT ON budget_expenses
FOR EACH ROW
BEGIN
    DECLARE current_balance DECIMAL(14,2);

    -- Lấy số dư hiện tại
    SELECT cash_balance INTO current_balance
    FROM stores
    WHERE id = NEW.store_id
    FOR UPDATE;

    -- Nếu số dư < số tiền chi → báo lỗi (rollback)
    IF current_balance < NEW.amount THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insufficient budget balance for this expense';
    END IF;

    -- Trừ tiền ngân sách
    UPDATE stores
    SET cash_balance = cash_balance - NEW.amount
    WHERE id = NEW.store_id;
END $$

DELIMITER ;
