package com.example.storemanagement.util;

import com.example.storemanagement.dao.DBConnection;
import java.sql.Connection;
import java.sql.Statement;

public class Migration {
    public static void main(String[] args) {
        String sql = "CREATE TABLE IF NOT EXISTS supplier_product_prices (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "product_id BIGINT NOT NULL, " +
                "supplier_id BIGINT NOT NULL, " +
                "import_price DECIMAL(15,2) NOT NULL, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (product_id) REFERENCES products(id), " +
                "FOREIGN KEY (supplier_id) REFERENCES suppliers(id), " +
                "UNIQUE KEY unique_price (product_id, supplier_id)" +
                ")";

        try (Connection cn = DBConnection.getInstance().getConnection();
                Statement stmt = cn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Migration successful: supplier_product_prices table created.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
