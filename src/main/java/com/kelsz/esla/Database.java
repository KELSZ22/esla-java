package com.kelsz.esla;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class Database {

    // 🔌 SQLite CONFIG (HARDCODED)
    private static final String URL = "jdbc:sqlite:esla.db";
    private static boolean isInitialized = false;

    private Database() {
        // prevent instantiation
    }

    public static synchronized Connection getConnection() {
        try {
            // Explicitly load the SQLite driver class
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection(URL);
            
            // Set SQLite Pragmas for better concurrency and performance
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys=ON;");
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA synchronous=NORMAL;");
                stmt.execute("PRAGMA busy_timeout=5000;");
            }
            
            if (!isInitialized) {
                initializeDatabase(connection);
                isInitialized = true;
            }
            
            System.out.println("✅ SQLite Connected!");
            return connection;
        } catch (ClassNotFoundException e) {
            System.out.println("❌ SQLite Driver not found! Ensure it's in your POM/Classpath.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.out.println("❌ DB Connection Failed");
            e.printStackTrace();
            return null;
        }
    }

    private static void initializeDatabase(Connection conn) {
        System.out.println("🔄 Initializing Database Schema...");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL" +
                ")");

            // Insert a default user if none exists
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // admin / password
                    String defaultPasswordHash = org.mindrot.jbcrypt.BCrypt.hashpw("password", org.mindrot.jbcrypt.BCrypt.gensalt());
                    stmt.execute("INSERT INTO users (name, email, password) VALUES ('Admin', 'admin@example.com', '" + defaultPasswordHash + "')");
                }
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS members (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone TEXT NOT NULL, " +
                "address TEXT NOT NULL, " +
                "member_type TEXT NOT NULL, " +
                "member_since DATE NOT NULL, " +
                "premium INTEGER NOT NULL DEFAULT 0, " +
                "deleted_at TIMESTAMP, " +
                "created_at TIMESTAMP, " +
                "updated_at TIMESTAMP" +
                ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS ledgers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "description TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "date DATE NOT NULL, " +
                "deleted_at TIMESTAMP, " +
                "created_at TIMESTAMP, " +
                "updated_at TIMESTAMP" +
                ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS form_data (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ledger_id INTEGER, " +
                "member_id INTEGER, " +
                "form_number INTEGER, " +
                "is_loan BOOLEAN NOT NULL DEFAULT 0, " +
                "date DATE, " +
                "should_be_paid REAL, " +
                "actual_payment REAL, " +
                "balance REAL, " +
                "under_paid REAL, " +
                "scheduled_payment REAL, " +
                "premium_total REAL, " +
                "premium REAL, " +
                "actual_payroll REAL, " +
                "remarks TEXT, " +
                "deleted_at TIMESTAMP, " +
                "created_at TIMESTAMP, " +
                "updated_at TIMESTAMP, " +
                "FOREIGN KEY (ledger_id) REFERENCES ledgers(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE" +
                ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS loans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "form_data_id INTEGER, " +
                "ledger_id INTEGER, " +
                "member_id INTEGER, " +
                "form_number INTEGER, " +
                "date DATE, " +
                "start_deduction_on_loan_date BOOLEAN NOT NULL DEFAULT 0, " +
                "start_deduction_date DATE, " +
                "principal REAL, " +
                "service_charge REAL, " +
                "service_charge_balance REAL, " +
                "interest REAL, " +
                "interest_balance REAL, " +
                "cutoffs INTEGER, " +
                "cutoffs_amount REAL, " +
                "total REAL, " +
                "remarks TEXT, " +
                "deleted_at TIMESTAMP, " +
                "created_at TIMESTAMP, " +
                "updated_at TIMESTAMP, " +
                "FOREIGN KEY (form_data_id) REFERENCES form_data(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (ledger_id) REFERENCES ledgers(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE" +
                ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS member_service_charge_refunds (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "member_id INTEGER NOT NULL, " +
                "description TEXT NOT NULL, " +
                "date_from DATE NOT NULL, " +
                "date_to DATE NOT NULL, " +
                "deleted_at TIMESTAMP, " +
                "created_at TIMESTAMP, " +
                "updated_at TIMESTAMP, " +
                "FOREIGN KEY (member_id) REFERENCES members(id)" +
                ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS member_service_charge_refund_forms (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "mscr_refund_id INTEGER NOT NULL, " +
                "form_number INTEGER, " +
                "date_loan DATE, " +
                "principal REAL, " +
                "interest REAL, " +
                "service_charge REAL, " +
                "total REAL, " +
                "no_of_months REAL, " +
                "collected_interest REAL, " +
                "total_interest REAL, " +
                "refund_60 REAL, " +
                "refund_40 REAL, " +
                "remarks TEXT, " +
                "has_balance BOOLEAN NOT NULL DEFAULT 0, " +
                "deleted_at TIMESTAMP, " +
                "created_at TIMESTAMP, " +
                "updated_at TIMESTAMP, " +
                "FOREIGN KEY (mscr_refund_id) REFERENCES member_service_charge_refunds(id) ON DELETE CASCADE" +
                ")");

            System.out.println("✅ Database Schema Initialized!");
        } catch (SQLException e) {
            System.out.println("❌ DB Initialization Failed");
            e.printStackTrace();
        }
    }
}