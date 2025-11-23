DELIMITER $$

DROP TRIGGER IF EXISTS trg_allocation_after_insert $$

CREATE TRIGGER trg_allocation_after_insert
AFTER INSERT ON budget_allocations
FOR EACH ROW
BEGIN
    UPDATE stores
    SET cash_balance = cash_balance + NEW.amount
    WHERE id = NEW.store_id;
END $$

DELIMITER ;
