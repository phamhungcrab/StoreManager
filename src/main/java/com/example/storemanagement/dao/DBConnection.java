package com.example.storemanagement.dao;

/*
 * DBConnection – Singleton quản lý kết nối MySQL bằng JDBC.
 * - Đọc cấu hình từ classpath: /database/db.properties (url, user, password)
 * - Cho phép fallback biến môi trường: DB_URL, DB_USER, DB_PASSWORD.
 * - Dùng try-with-resources ở DAO để tự đóng Connection/Statement/ResultSet.
 */

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class DBConnection {
    private static volatile DBConnection instance;
    private final Properties props = new Properties();

    private DBConnection() {
        loadProps();
        // Đảm bảo driver được nạp (MySQL 8: com.mysql.cj.jdbc.Driver). Không bắt buộc với JDBC 4+, nhưng an toàn.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) instance = new DBConnection();
            }
        }
        return instance;
    }

    private void loadProps() {
        // 1) Từ classpath
        try (InputStream in = getClass().getResourceAsStream("/database/db.properties")) {
            if (in != null) props.load(in);
        } catch (Exception e) {
            System.err.println("[DB] Không đọc được /database/db.properties: " + e.getMessage());
        }
        // 2) Fallback ENV
        props.putIfAbsent("url", System.getenv("DB_URL"));
        props.putIfAbsent("user", System.getenv("DB_USER"));
        props.putIfAbsent("password", System.getenv("DB_PASSWORD"));
        // 3) Giá trị mặc định an toàn
        props.putIfAbsent("url", "jdbc:mysql://localhost:3306/store_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        props.putIfAbsent("user", "root");
        props.putIfAbsent("password", "");
    }

    public Connection getConnection() throws SQLException {
        String url = Objects.toString(props.getProperty("url"), "");
        String user = Objects.toString(props.getProperty("user"), "");
        String pass = Objects.toString(props.getProperty("password"), "");
        return DriverManager.getConnection(url, user, pass);
    }
}