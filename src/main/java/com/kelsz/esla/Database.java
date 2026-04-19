package com.kelsz.esla;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    // 🔌 MySQL CONFIG (HARDCODED)
    private static final String URL = "jdbc:mysql://localhost:3306/esla";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Database() {
        // prevent instantiation
    }

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ MySQL Connected!");
            return connection;
        } catch (SQLException e) {
            System.out.println("❌ DB Connection Failed");
            e.printStackTrace();
            return null;
        }
    }
}