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
        seedAmvi();
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

    private static void seedAmvi() {
        System.out.println("💰 Seeding FormData from FormDataSeeder for Amvi...");
        int memberId = 1;
        int ledgerId = 1;
        String ledgerType = "channel 3";
        try (Connection conn = Database.getConnection()) {
            try(Statement stmt = conn.createStatement(); java.sql.ResultSet rs = stmt.executeQuery("SELECT id FROM ledgers WHERE type = 'channel 3' LIMIT 1")) {
                if(rs.next()) ledgerId = rs.getInt(1);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        // Delete Amvi's random data so we start fresh for this member
        try (Connection conn = Database.getConnection()) {
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM form_data WHERE member_id = ?")) {
                ps.setInt(1, memberId);
                ps.executeUpdate();
            }
            try(PreparedStatement ps = conn.prepareStatement("DELETE FROM loans WHERE member_id = ?")) {
                ps.setInt(1, memberId);
                ps.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        services.PaymentService paymentService = new services.PaymentService();
        String[][] records = {
            {"2019-01-15", null, "500"},
            {"2019-01-30", null, "500"},
            {"2019-02-15", null, "500"},
            {"2019-02-28", null, "500"},
            {"2019-03-15", null, "500"},
            {"2019-03-30", null, "500"},
            {"2019-04-15", null, "500"},
            {"2019-04-30", "570", "500"},
            {"2019-05-15", "570", "500"},
            {"2019-05-30", "1312.5", "500"},
            {"2019-06-15", "817.5", "500"},
            {"2019-06-30", "1023.75", "500"},
            {"2019-07-15", "1023.75", "500"},
            {"2019-07-30", "1023.75", "500"},
            {"2019-08-15", "1023.75", "500"},
            {"2019-08-30", "1023.75", "500"},
            {"2019-09-15", "1023.75", "500"},
            {"2019-09-30", "2223.75", "500"},
            {"2019-10-15", "2223.75", "500"},
            {"2019-10-30", "1200", "500"},
            {"2019-11-15", "1800", "500"},
            {"2019-11-30", "1800", "500"},
            {"2019-12-15", "1800", "500"},
            {"2019-12-30", "1800", "500"},
            {"2020-01-15", "1800", "500"},
            {"2020-01-30", "2750", "500"},
            {"2020-02-15", "4112.5", "500"},
            {"2020-02-28", "2912.5", "500"},
            {"2020-03-15", "2912.5", "500"},
            {"2020-03-30", "500", null},
            {"2020-04-15", null, null},
            {"2020-04-30", null, null},
            {"2020-05-15", null, null},
            {"2020-05-30", null, null},
            {"2020-06-15", null, null},
            {"2020-06-30", null, null},
            {"2020-07-15", null, null},
            {"2020-07-30", "3362.5", "500"},
            {"2020-08-15", "1025", "500"},
            {"2020-08-30", "1025", "500"},
            {"2020-09-15", "1025", "500"},
            {"2020-09-30", "1025", "500"},
            {"2020-10-15", "1525", null},
            {"2020-10-30", "1525", null},
            {"2020-11-15", "2475", null},
            {"2020-11-30", "1525", null},
            {"2020-12-15", "1525", null},
            {"2020-12-30", "2475", null},
            {"2021-01-15", "1525", null},
            {"2021-01-30", "1375", null},
            {"2021-02-15", "1025", null},
            {"2021-02-28", "1890.91", null},
            {"2021-03-15", "1890.91", null},
            {"2021-03-30", "1890.91", null},
            {"2021-04-15", "1890.91", null},
            {"2021-04-30", "1765.91", null},
            {"2021-05-15", "940.91", null},
            {"2021-05-30", "1818.41", null},
            {"2021-06-15", "1818.41", null},
            {"2021-06-30", "1818.41", null},
            {"2021-07-15", "2298.41", null},
            {"2021-07-30", "2298.41", null},
            {"2021-08-15", "2298.41", null},
            {"2021-08-30", "2298.41", null},
            {"2021-09-15", "3760.91", null},
            {"2021-09-30", "2883.41", null},
            {"2021-10-15", "3614.66", null},
            {"2021-10-30", "3614.66", null},
            {"2021-11-15", "3614.66", null},
            {"2021-11-30", "4754.66", null},
            {"2021-12-15", "4274.66", null},
            {"2021-12-30", "4274.66", null},
            {"2022-01-15", "2812.16", null},
            {"2022-01-30", "3921.25", null},
            {"2022-02-15", "3190", "1000"},
            {"2022-02-28", "2050", "1000"},
            {"2022-03-15", "2650", "400"},
            {"2022-03-30", "2650", "1600"},
            {"2022-04-15", "3235", "1000"},
            {"2022-04-30", "3955", "1000"},
            {"2022-05-15", "4675", "1600"},
            {"2022-05-30", "4675", "1000"},
            {"2022-06-15", "5875", "1000"},
            {"2022-06-30", "5875", "1000"},
            {"2022-07-15", "5875", "1000"},
            {"2022-07-30", "5875", "1000"},
            {"2022-08-15", "4310", "1000"},
            {"2022-08-30", "4680", "1000"},
            {"2022-09-15", "6300", "1000"},
            {"2022-09-30", "5580", "1000"},
            {"2022-10-15", "4860", "1000"},
            {"2022-10-30", "6937.5", "1000"},
            {"2022-11-15", "5737.50", "1000"},
            {"2022-11-30", "6457.50", "280"},
            {"2022-12-15", "6457.50", "1720"},
            {"2022-12-30", "4777.50", "1000"},
            {"2023-01-15", "6817.50", "2000"},
            {"2023-01-30", "8867.50", "2000"},
            {"2023-02-15", "8125.00", "2000"},
            {"2023-02-28", "7247.50", "2000"},
            {"2023-03-15", "8687.50", "2000"},
            {"2023-03-30", "7487.50", "2000"},
            {"2023-04-15", "8205.00", "2000"},
            {"2023-04-30", "6388.70", null},
            {"2023-05-15", "11840.68", "2000"},
            {"2023-05-30", "7535.48", null},
            {"2023-06-15", "10758.28", "2000"},
            {"2023-06-30", "7764.38", "2000"},
            {"2023-07-15", "7764.38", "2000"},
            {"2023-07-30", "7847.11", "26.49"},
            {"2023-08-15", "7847.11", "2000"},
            {"2023-08-30", "8327.11", "2000"},
            {"2023-09-15", "8807.11", "2000"},
            {"2023-09-30", "9873.47", "2000"},
            {"2023-10-15", "7715.97", "2000"},
            {"2023-10-30", "7203.00", "2000"},
            {"2023-11-15", "7203.94", "2000"},
            {"2023-11-30", "8654.72", "2000"},
            {"2023-12-15", "8787.22", "1147.78"},
            {"2023-12-31", "8787.22", "2000"}
        };
        for (String[] rec : records) {
            LocalDate date = LocalDate.parse(rec[0]);
            java.math.BigDecimal actualPayment = rec[1] == null ? null : new java.math.BigDecimal(rec[1]);
            java.math.BigDecimal premium = rec[2] == null ? null : new java.math.BigDecimal(rec[2]);
            paymentService.processPayment(ledgerId, memberId, ledgerType, date, null, premium, actualPayment, null);
        }
    }
}
