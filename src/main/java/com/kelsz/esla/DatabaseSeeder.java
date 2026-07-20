package com.kelsz.esla;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseSeeder {

    private static final Random random = new Random();
    private static final String DEFAULT_PHONE = "1234567890";
    private static final String DEFAULT_ADDRESS = "123 Main St, Anytown, USA";
    private static final int DEFAULT_PREMIUM = 0;

    private record MemberSeed(String name, String email, String phone, String address, String memberType) {}

    private static final MemberSeed[] MEMBER_SEEDS = {
        new MemberSeed("Abellar, Amvi", "amvi@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Antonio, Marlene", "marlene@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Aparejado, Angela", "angela@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Basas, Narciso", "narciso@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Bepinosa, Elmer", "elmer@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Bitanga, Arnold", "arnold@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Bumagat, Mark Lewis", "marklewis@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Cajayon, Cynea", "cynea@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Competente, Crecenciano", "crecenciano@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Delos Santos, Josmar", "josmar@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Delotendo, Val", "val@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("De Vera, Michael", "michael@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Garcia, Caren", "caren@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Joson, Michael", "joson@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Liclican, Lawrence", "lawrence@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Luna, Byron", "byron@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Maico, Redgie", "redgie@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Malinao, Harold", "harold@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Mantilla, Icyl", "icyl@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Mayoya, Genevieve", "genevieve@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Nacion, Ferdie", "ferdie@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Padamada, Jaypee", "jaypee@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Pelovello, Cherrywell", "cherrywel@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Periabras, Nino", "nino@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Quinay, Micah", "micah@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Reamillio, Therese Ann", "thereseann@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Rizardo, Dondie", "dondi@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Saddie, Jannel", "jannel@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Salcedo, Jimmy", "jimmy@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Santiago, Michael Vincent", "michaelvincent@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Santos, Elmer", "elmer2@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Soliman, Jay", "jay@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Untalan, Karla", "karla@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Valdesotto, Aedrian", "aedrian@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Valenzuela, Alex", "alex@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Zamora, Albert", "albert@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "channel 3"),
        new MemberSeed("Arsenio, Loida", "loida@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Borgonia, Glenda", "glen@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Bullo, Dexter", "dexter@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Dela Serna, Marry Ann", "marryann@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Lubis, Dennis", "dennis@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Maddawin, Shanani", "shanan@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Magbitang, Rainer", "rainer@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Mercado, Myrna", "myrna@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Piga, Justa", "justa@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Ramos, Jun", "jun@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Sabino, Cely Mae", "celymae@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Subebe, Ralp", "ralp@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Teope, Tess", "janella@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "resort"),
        new MemberSeed("Bagasona Adriano", "adriano@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "executive"),
        new MemberSeed("Calangi Adel", "adel@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "executive"),
        new MemberSeed("Quinones, Clemente", "clemente@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "executive"),
        new MemberSeed("Riel, Rosalinda", "rosalinda@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "executive"),
        new MemberSeed("Cadiz, Cynthia", "cynthia@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "consultant"),
        new MemberSeed("Cerbania, Robert", "robert@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "consultant"),
        new MemberSeed("Frilles, Edwin", "edwin@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "consultant"),
        new MemberSeed("Mercado, Herminio", "herminio@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "consultant"),
        new MemberSeed("Quinay, Mercy", "mercy@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "consultant"),
        new MemberSeed("Quinay, Milo", "milo@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "consultant"),
        new MemberSeed("Sales, Reynaldo", "reynaldo@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "consultant"),
        new MemberSeed("Sinson, Pinky", "pinky@example.com", DEFAULT_PHONE, DEFAULT_ADDRESS, "consultant")
    };

    public static void main(String[] args) {
        System.out.println("🌱 Starting Database Seeding...");
        seed();
        System.out.println("✅ Seeding Completed!");
    }

    public static void seed() {
        try (Connection conn = Database.getConnection()) {
            if (conn == null) return;

            conn.setAutoCommit(false);
            try {
                // Clear existing data (in reverse order of dependencies).
                System.out.println("🗑️ Clearing existing data...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                    stmt.execute("DELETE FROM member_service_charge_refund_forms");
                    stmt.execute("DELETE FROM member_service_charge_refunds");
                    stmt.execute("DELETE FROM loans");
                    stmt.execute("DELETE FROM form_data");
                    stmt.execute("DELETE FROM ledgers");
                    stmt.execute("DELETE FROM members");
                    stmt.execute("DELETE FROM sqlite_sequence WHERE name IN ('members', 'ledgers', 'form_data', 'loans', 'member_service_charge_refunds', 'member_service_charge_refund_forms')");
                }

                // 1. Seed Members
                System.out.println("👤 Seeding Members...");
                List<Integer> memberIds = new ArrayList<>();
                for (MemberSeed member : MEMBER_SEEDS) {
                    int memberId = insertMember(conn, member.name(), member.email(),
                                     member.phone(), member.address(), member.memberType(),
                                     Date.valueOf(LocalDate.now()), DEFAULT_PREMIUM);
                    memberIds.add(memberId);
                }

                // 2. Seed Ledgers
                System.out.println("📔 Seeding Ledgers...");
                List<Integer> ledgerIds = new ArrayList<>();
                com.kelsz.esla.enums.MemberType[] ledgerTypes = com.kelsz.esla.enums.MemberType.values();
                for (int i = 1; i <= ledgerTypes.length; i++) {
                    int ledgerId = insertLedger(conn, ledgerTypes[i-1].toString().toUpperCase() + " Batch " + LocalDate.now().getYear(),
                                     ledgerTypes[i-1].getValue(),
                                     Date.valueOf(LocalDate.now().minusDays(i * 30)));
                    ledgerIds.add(ledgerId);
                }

                // 3. Seed Form Data & Loans
                System.out.println("💰 Seeding Form Data and Loans...");
                for (int memberId : memberIds) {
                    // Each member has 1-3 payments/loans.
                    int numEntries = 1 + random.nextInt(3);
                    for (int j = 0; j < numEntries; j++) {
                        int ledgerId = ledgerIds.get(random.nextInt(ledgerIds.size()));
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

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int insertMember(Connection conn, String name, String email, String phone, String address, String type, Date since, int premium) throws SQLException {
        String sql = "INSERT INTO members (name, email, phone, address, member_type, member_since, premium, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, type);
            ps.setDate(6, since);
            ps.setInt(7, premium);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to insert member: " + email);
    }

    private static int insertLedger(Connection conn, String desc, String type, Date date) throws SQLException {
        String sql = "INSERT INTO ledgers (description, type, date, created_at, updated_at) VALUES (?, ?, ?, datetime('now'), datetime('now'))";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, desc);
            ps.setString(2, type);
            ps.setDate(3, date);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to insert ledger: " + desc);
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
