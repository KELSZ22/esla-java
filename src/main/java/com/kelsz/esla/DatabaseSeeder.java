package com.kelsz.esla;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.sql.Date;
import java.util.Random;

public class DatabaseSeeder {

    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("🌱 Starting Database Seeding...");
        seed();
        System.out.println("✅ Seeding Completed!");
    }

    public static void seed() {
        try (Connection conn = Database.getConnection()) {
            if (conn == null) return;

            // Clear existing data (in reverse order of dependencies)
            System.out.println("🗑️ Clearing existing data...");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM member_service_charge_refund_forms");
                stmt.execute("DELETE FROM member_service_charge_refunds");
                stmt.execute("DELETE FROM loans");
                stmt.execute("DELETE FROM form_data");
                stmt.execute("DELETE FROM ledgers");
                stmt.execute("DELETE FROM members");
                // Reset auto-increment
                stmt.execute("DELETE FROM sqlite_sequence WHERE name IN ('members', 'ledgers', 'form_data', 'loans', 'member_service_charge_refunds', 'member_service_charge_refund_forms')");
            }

            // 1. Seed Members
            System.out.println("👤 Seeding Members...");
            com.kelsz.esla.enums.MemberType[] memberTypes = com.kelsz.esla.enums.MemberType.values();
            for (int i = 1; i <= 20; i++) {
                insertMember(conn, "Member " + i, "member" + i + "@example.com", 
                             "0912345678" + i, "Address " + i, 
                             memberTypes[random.nextInt(memberTypes.length)].getValue(), 
                             Date.valueOf(LocalDate.now().minusMonths(random.nextInt(24))), 
                             500 + random.nextInt(1500));
            }

            // 2. Seed Ledgers
            System.out.println("📔 Seeding Ledgers...");
            com.kelsz.esla.enums.MemberType[] ledgerTypes = com.kelsz.esla.enums.MemberType.values();
            for (int i = 1; i <= ledgerTypes.length; i++) {
                insertLedger(conn, ledgerTypes[i-1].toString().toUpperCase() + " Batch " + LocalDate.now().getYear(), 
                             ledgerTypes[i-1].getValue(), 
                             Date.valueOf(LocalDate.now().minusDays(i * 30)));
            }

            // 3. Seed Form Data & Loans
            System.out.println("💰 Seeding Form Data and Loans...");
            for (int memberId = 1; memberId <= 20; memberId++) {
                // Each member has 1-3 payments/loans
                int numEntries = 1 + random.nextInt(3);
                for (int j = 0; j < numEntries; j++) {
                    int ledgerId = 1 + random.nextInt(5);
                    double principal = 5000 + random.nextInt(45000);
                    double interest = principal * 0.05;
                    double serviceCharge = principal * 0.01;
                    double total = principal + interest + serviceCharge;
                    
                    int formId = insertFormData(conn, ledgerId, memberId, 100 + j, 
                                               principal, principal, 0.0, 0.0, principal);
                    
                    if (random.nextBoolean()) {
                        insertLoan(conn, formId, ledgerId, memberId, 100 + j, principal, serviceCharge, interest, total);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertMember(Connection conn, String name, String email, String phone, String address, String type, Date since, int premium) throws SQLException {
        String sql = "INSERT INTO members (name, email, phone, address, member_type, member_since, premium, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, type);
            ps.setDate(6, since);
            ps.setInt(7, premium);
            ps.executeUpdate();
        }
    }

    private static void insertLedger(Connection conn, String desc, String type, Date date) throws SQLException {
        String sql = "INSERT INTO ledgers (description, type, date, created_at, updated_at) VALUES (?, ?, ?, datetime('now'), datetime('now'))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desc);
            ps.setString(2, type);
            ps.setDate(3, date);
            ps.executeUpdate();
        }
    }

    private static int insertFormData(Connection conn, int ledgerId, int memberId, int formNum, double shouldBePaid, double actualPay, double balance, double underPaid, double schedPay) throws SQLException {
        String sql = "INSERT INTO form_data (ledger_id, member_id, form_number, date, should_be_paid, actual_payment, balance, under_paid, scheduled_payment, created_at, updated_at) VALUES (?, ?, ?, date('now'), ?, ?, ?, ?, ?, datetime('now'), datetime('now'))";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ledgerId);
            ps.setInt(2, memberId);
            ps.setInt(3, formNum);
            ps.setDouble(4, shouldBePaid);
            ps.setDouble(5, actualPay);
            ps.setDouble(6, balance);
            ps.setDouble(7, underPaid);
            ps.setDouble(8, schedPay);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private static void insertLoan(Connection conn, int formId, int ledgerId, int memberId, int formNum, double principal, double sc, double interest, double total) throws SQLException {
        String sql = "INSERT INTO loans (form_data_id, ledger_id, member_id, form_number, date, principal, service_charge, interest, total, cutoffs, cutoffs_amount, created_at, updated_at) VALUES (?, ?, ?, ?, date('now'), ?, ?, ?, ?, 12, ?, datetime('now'), datetime('now'))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, formId);
            ps.setInt(2, ledgerId);
            ps.setInt(3, memberId);
            ps.setInt(4, formNum);
            ps.setDouble(5, principal);
            ps.setDouble(6, sc);
            ps.setDouble(7, interest);
            ps.setDouble(8, total);
            ps.setDouble(9, total / 12);
            ps.executeUpdate();
        }
    }
}
