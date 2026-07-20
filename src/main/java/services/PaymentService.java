package services;

import com.kelsz.esla.Database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Payment Business Logic Service
 * Handles all payment calculations and record creation according to ESLA business rules
 */
public class PaymentService {
    private LoanService loanService = new LoanService();

    /**
     * Represents computed payment fields
     */
    public static class ComputedFields {
        public BigDecimal interest;
        public BigDecimal serviceCharge;
        public BigDecimal total;
        public BigDecimal scheduledPayment;
        public BigDecimal shouldBePaid;
        public BigDecimal balance;
        public BigDecimal underPaid;
        public BigDecimal premiumTotal;
        public BigDecimal actualPayroll;
    }

    /**
     * Represents a previous entry from form_data
     */
    public static class PreviousEntry {
        public int id;
        public int ledgerId;
        public int memberId;
        public Integer formNumber;
        public Boolean isLoan;
        public LocalDate date;
        public BigDecimal shouldBePaid;
        public BigDecimal actualPayment;
        public BigDecimal balance;
        public BigDecimal underPaid;
        public BigDecimal scheduledPayment;
        public BigDecimal premiumTotal;
        public BigDecimal premium;
        public BigDecimal actualPayroll;
        public String remarks;
    }

    /**
     * Represents loan information
     */
    public static class LoanInfo {
        public int id;
        public int ledgerId;
        public int memberId;
        public Integer formNumber;
        public LocalDate date;
        public Boolean startDeductionOnLoanDate;
        public LocalDate startDeductionDate;
        public BigDecimal principal;
        public BigDecimal serviceCharge;
        public BigDecimal serviceChargeBalance;
        public BigDecimal interest;
        public BigDecimal interestBalance;
        public Integer cutoffs;
        public BigDecimal cutoffsAmount;
        public BigDecimal total;
        public String remarks;
    }

    /**
     * Find the most recent prior form_data entry for continuity (same ledger type).
     * Prefers entries strictly before {@code beforeDate}. Falls back to any earlier row
     * when {@code beforeDate} is null.
     */
    public PreviousEntry findPreviousEntry(int ledgerId, int memberId, String ledgerType) {
        return findPreviousEntry(memberId, ledgerType, null, -1);
    }

    /**
     * @param beforeOrOnDate when non-null, only consider entries with date &lt;= this date
     * @param excludeId      form_data id to skip (current row when editing), or -1
     */
    public PreviousEntry findPreviousEntry(int memberId, String ledgerType, LocalDate beforeOrOnDate, int excludeId) {
        PreviousEntry previousEntry = null;
        try {
            Connection con = Database.getConnection();
            StringBuilder sql = new StringBuilder("""
                SELECT fd.id, fd.ledger_id, fd.member_id, fd.form_number, fd.is_loan, fd.date,
                       fd.should_be_paid, fd.actual_payment, fd.balance, fd.under_paid,
                       fd.scheduled_payment, fd.premium_total, fd.premium, fd.actual_payroll, fd.remarks
                FROM form_data fd
                INNER JOIN ledgers l ON fd.ledger_id = l.id
                WHERE l.type = ? AND fd.member_id = ?
                  AND fd.deleted_at IS NULL AND l.deleted_at IS NULL
                """);
            if (beforeOrOnDate != null) {
                sql.append(" AND fd.date <= ?");
            }
            if (excludeId > 0) {
                sql.append(" AND fd.id != ?");
            }
            sql.append(" ORDER BY fd.date DESC, fd.id DESC LIMIT 1");

            PreparedStatement ps = con.prepareStatement(sql.toString());
            int idx = 1;
            ps.setString(idx++, ledgerType);
            ps.setInt(idx++, memberId);
            if (beforeOrOnDate != null) {
                ps.setString(idx++, beforeOrOnDate.toString());
            }
            if (excludeId > 0) {
                ps.setInt(idx++, excludeId);
            }
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                previousEntry = mapPreviousEntry(rs);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return previousEntry;
    }

    /**
     * For create: prior entry must be strictly before the new payment date so back-dated
     * inserts chain off the correct predecessor (not a later existing row).
     */
    public PreviousEntry findPreviousEntryBefore(int memberId, String ledgerType, LocalDate beforeDate, int excludeId) {
        PreviousEntry previousEntry = null;
        try {
            Connection con = Database.getConnection();
            StringBuilder sql = new StringBuilder("""
                SELECT fd.id, fd.ledger_id, fd.member_id, fd.form_number, fd.is_loan, fd.date,
                       fd.should_be_paid, fd.actual_payment, fd.balance, fd.under_paid,
                       fd.scheduled_payment, fd.premium_total, fd.premium, fd.actual_payroll, fd.remarks
                FROM form_data fd
                INNER JOIN ledgers l ON fd.ledger_id = l.id
                WHERE l.type = ? AND fd.member_id = ?
                  AND fd.deleted_at IS NULL AND l.deleted_at IS NULL
                  AND fd.date < ?
                """);
            if (excludeId > 0) {
                sql.append(" AND fd.id != ?");
            }
            sql.append(" ORDER BY fd.date DESC, fd.id DESC LIMIT 1");

            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, ledgerType);
            ps.setInt(2, memberId);
            ps.setString(3, beforeDate != null ? beforeDate.toString() : "9999-12-31");
            if (excludeId > 0) {
                ps.setInt(4, excludeId);
            }
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                previousEntry = mapPreviousEntry(rs);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return previousEntry;
    }

    private PreviousEntry mapPreviousEntry(ResultSet rs) throws SQLException {
        PreviousEntry previousEntry = new PreviousEntry();
        previousEntry.id = rs.getInt("id");
        previousEntry.ledgerId = rs.getInt("ledger_id");
        previousEntry.memberId = rs.getInt("member_id");
        previousEntry.formNumber = rs.getObject("form_number", Integer.class);
        previousEntry.isLoan = rs.getObject("is_loan", Boolean.class);
        String dateStr = rs.getString("date");
        previousEntry.date = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(dateStr);
        previousEntry.shouldBePaid = rs.getBigDecimal("should_be_paid");
        previousEntry.actualPayment = rs.getBigDecimal("actual_payment");
        previousEntry.balance = rs.getBigDecimal("balance");
        previousEntry.underPaid = rs.getBigDecimal("under_paid");
        previousEntry.scheduledPayment = rs.getBigDecimal("scheduled_payment");
        previousEntry.premiumTotal = rs.getBigDecimal("premium_total");
        previousEntry.premium = rs.getBigDecimal("premium");
        previousEntry.actualPayroll = rs.getBigDecimal("actual_payroll");
        previousEntry.remarks = rs.getString("remarks");
        return previousEntry;
    }

    /**
     * 2. Determine Premium Total Baseline
     */
    public BigDecimal determinePremiumTotalBaseline(int memberId, PreviousEntry previousEntry) {
        if (previousEntry != null && previousEntry.premiumTotal != null) {
            return previousEntry.premiumTotal;
        }

        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT premium_total
                FROM form_data
                WHERE member_id = ? AND premium_total IS NOT NULL AND premium_total > 0
                  AND deleted_at IS NULL
                ORDER BY date DESC, id DESC
                LIMIT 1
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            BigDecimal baseline = null;
            if (rs.next()) {
                baseline = rs.getBigDecimal("premium_total");
            }

            rs.close();
            ps.close();
            con.close();
            return baseline;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 3. Compute Fields
     */
    public ComputedFields computeFields(
            int ledgerId, int memberId, String ledgerType, LocalDate date,
            BigDecimal scheduledPayment, BigDecimal premium, BigDecimal actualPayment, String remarks,
            PreviousEntry previousEntry, BigDecimal premiumTotalBaseline) {

        ComputedFields computed = new ComputedFields();

        LoanInfo loanInfo = getLoanInfo(ledgerId, memberId, ledgerType, date);

        // Prefer stored interest amount; otherwise compute from rate (interest_balance ~ 0.03)
        if (loanInfo != null && loanInfo.interest != null) {
            computed.interest = loanInfo.interest;
        } else if (loanInfo != null && loanInfo.principal != null && loanInfo.interestBalance != null && loanInfo.cutoffs != null) {
            BigDecimal rate = normalizeRate(loanInfo.interestBalance);
            computed.interest = loanInfo.principal
                .multiply(rate)
                .multiply(BigDecimal.valueOf(loanInfo.cutoffs));
        } else {
            computed.interest = BigDecimal.ZERO;
        }

        if (loanInfo != null && loanInfo.serviceCharge != null) {
            computed.serviceCharge = loanInfo.serviceCharge;
        } else if (loanInfo != null && loanInfo.principal != null && loanInfo.serviceChargeBalance != null) {
            computed.serviceCharge = loanInfo.principal.multiply(normalizeRate(loanInfo.serviceChargeBalance));
        } else {
            computed.serviceCharge = BigDecimal.ZERO;
        }

        if (loanInfo != null && loanInfo.total != null) {
            computed.total = loanInfo.total;
        } else if (loanInfo != null) {
            BigDecimal principal = loanInfo.principal != null ? loanInfo.principal : BigDecimal.ZERO;
            computed.total = principal
                .add(computed.interest)
                .add(computed.serviceCharge);
        } else {
            computed.total = BigDecimal.ZERO;
        }

        // When caller supplies scheduled payment (edit/recompute), keep it; else derive from loans
        if (scheduledPayment != null) {
            computed.scheduledPayment = scheduledPayment;
        } else {
            computed.scheduledPayment = loanService.getScheduledPaymentWithDate(ledgerId, memberId, ledgerType, date);
        }

        BigDecimal previousUnderPaid = (previousEntry != null && previousEntry.underPaid != null)
            ? previousEntry.underPaid
            : BigDecimal.ZERO;
        computed.shouldBePaid = previousUnderPaid.add(
            computed.scheduledPayment != null ? computed.scheduledPayment : BigDecimal.ZERO);

        BigDecimal previousBalance = (previousEntry != null && previousEntry.balance != null)
            ? previousEntry.balance
            : BigDecimal.ZERO;

        computed.balance = previousBalance
            .subtract(actualPayment != null ? actualPayment : BigDecimal.ZERO)
            .add(computed.total);

        BigDecimal actualPaymentValue = actualPayment != null ? actualPayment : BigDecimal.ZERO;
        if (actualPaymentValue.compareTo(computed.shouldBePaid) < 0) {
            computed.underPaid = computed.shouldBePaid.subtract(actualPaymentValue);
        } else {
            computed.underPaid = BigDecimal.ZERO;
        }

        BigDecimal previousPremiumTotal = premiumTotalBaseline != null ? premiumTotalBaseline : BigDecimal.ZERO;
        computed.premiumTotal = previousPremiumTotal.add(premium != null ? premium : BigDecimal.ZERO);

        computed.actualPayroll = (actualPayment != null ? actualPayment : BigDecimal.ZERO)
            .add(premium != null ? premium : BigDecimal.ZERO);

        return computed;
    }

    /**
     * If a legacy loan stored a dollar amount in *_balance, treat it as already-applied and use 0 rate
     * for recomputation from principal. Rates are expected in (0, 1].
     */
    private BigDecimal normalizeRate(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ONE) > 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    public int createPaymentRecord(
            int ledgerId, int memberId, String ledgerType, LocalDate date,
            BigDecimal scheduledPayment, BigDecimal premium, BigDecimal actualPayment, String remarks,
            ComputedFields computed) {

        int formNumber = generateFormNumber(ledgerId);

        try {
            Connection con = Database.getConnection();
            String sql = """
                INSERT INTO form_data (
                    ledger_id, member_id, form_number, is_loan, date,
                    scheduled_payment, premium, actual_payment, remarks,
                    should_be_paid, balance, under_paid, premium_total, actual_payroll
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, ledgerId);
            ps.setInt(2, memberId);
            ps.setInt(3, formNumber);
            ps.setBoolean(4, false);
            ps.setString(5, date != null ? date.toString() : null);
            ps.setBigDecimal(6, scheduledPayment != null ? scheduledPayment : computed.scheduledPayment);
            ps.setBigDecimal(7, premium);
            ps.setBigDecimal(8, actualPayment);
            ps.setString(9, remarks);
            ps.setBigDecimal(10, computed.shouldBePaid);
            ps.setBigDecimal(11, computed.balance);
            ps.setBigDecimal(12, computed.underPaid);
            ps.setBigDecimal(13, computed.premiumTotal);
            ps.setBigDecimal(14, computed.actualPayroll);

            int rowsAffected = ps.executeUpdate();

            int generatedId = -1;
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                }
                rs.close();
            }

            ps.close();
            con.close();
            return generatedId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public LoanInfo getLoanInfo(int ledgerId, int memberId, String ledgerType, LocalDate date) {
        LoanInfo loanInfo = null;
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT l.id, l.ledger_id, l.member_id, l.form_number, l.date,
                       l.start_deduction_on_loan_date, l.start_deduction_date,
                       l.principal, l.service_charge, l.service_charge_balance,
                       l.interest, l.interest_balance, l.cutoffs, l.cutoffs_amount, l.total, l.remarks
                FROM loans l
                INNER JOIN ledgers led ON l.ledger_id = led.id
                WHERE led.type = ? AND l.member_id = ? AND l.date = ?
                  AND l.deleted_at IS NULL AND led.deleted_at IS NULL
                LIMIT 1
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, ledgerType);
            ps.setInt(2, memberId);
            ps.setDate(3, date != null ? java.sql.Date.valueOf(date) : null);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                loanInfo = new LoanInfo();
                loanInfo.id = rs.getInt("id");
                loanInfo.ledgerId = rs.getInt("ledger_id");
                loanInfo.memberId = rs.getInt("member_id");
                loanInfo.formNumber = rs.getObject("form_number", Integer.class);

                String dateStr = rs.getString("date");
                loanInfo.date = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(dateStr);

                loanInfo.startDeductionOnLoanDate = rs.getObject("start_deduction_on_loan_date", Boolean.class);

                String sddStr = rs.getString("start_deduction_date");
                loanInfo.startDeductionDate = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(sddStr);
                loanInfo.principal = rs.getBigDecimal("principal");
                loanInfo.serviceCharge = rs.getBigDecimal("service_charge");
                loanInfo.serviceChargeBalance = rs.getBigDecimal("service_charge_balance");
                loanInfo.interest = rs.getBigDecimal("interest");
                loanInfo.interestBalance = rs.getBigDecimal("interest_balance");
                loanInfo.cutoffs = rs.getObject("cutoffs", Integer.class);
                loanInfo.cutoffsAmount = rs.getBigDecimal("cutoffs_amount");
                loanInfo.total = rs.getBigDecimal("total");
                loanInfo.remarks = rs.getString("remarks");
            }

            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loanInfo;
    }

    public int calculatePosition(int ledgerId, int memberId, String ledgerType, LocalDate date) {
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT COUNT(*) + 1 as position
                FROM form_data fd
                INNER JOIN ledgers l ON fd.ledger_id = l.id
                WHERE l.type = ? AND fd.member_id = ? AND fd.date < ?
                  AND fd.deleted_at IS NULL AND l.deleted_at IS NULL
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, ledgerType);
            ps.setInt(2, memberId);
            ps.setDate(3, date != null ? java.sql.Date.valueOf(date) : null);
            ResultSet rs = ps.executeQuery();

            int position = 1;
            if (rs.next()) {
                position = rs.getInt("position");
            }

            rs.close();
            ps.close();
            con.close();
            return position;
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    private int generateFormNumber(int ledgerId) {
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT COALESCE(MAX(form_number), 0) + 1 as next_form_number
                FROM form_data
                WHERE ledger_id = ? AND deleted_at IS NULL
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, ledgerId);
            ResultSet rs = ps.executeQuery();

            int formNumber = 1;
            if (rs.next()) {
                formNumber = rs.getInt("next_form_number");
            }

            rs.close();
            ps.close();
            con.close();
            return formNumber;
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Create a payment and re-chain any later continuous-ledger rows for this member/type.
     */
    public int processPayment(
            int ledgerId, int memberId, String ledgerType, LocalDate date,
            BigDecimal scheduledPayment, BigDecimal premium, BigDecimal actualPayment, String remarks) {

        PreviousEntry previousEntry = findPreviousEntryBefore(memberId, ledgerType, date, -1);
        BigDecimal premiumTotalBaseline = determinePremiumTotalBaseline(memberId, previousEntry);

        ComputedFields computed = computeFields(
            ledgerId, memberId, ledgerType, date,
            scheduledPayment, premium, actualPayment, remarks,
            previousEntry, premiumTotalBaseline
        );

        int recordId = createPaymentRecord(
            ledgerId, memberId, ledgerType, date,
            scheduledPayment != null ? scheduledPayment : computed.scheduledPayment,
            premium, actualPayment, remarks,
            computed
        );

        if (recordId > 0) {
            recomputeMemberChain(memberId, ledgerType, date);
        }

        return recordId;
    }

    /**
     * Update a payment and recompute the continuous chain from that date forward.
     */
    public boolean updatePayment(
            int paymentId, int ledgerId, int memberId, String ledgerType, LocalDate date,
            BigDecimal scheduledPayment, BigDecimal premium, BigDecimal actualPayment, String remarks) {

        // Same-date predecessors (earlier id) must still count for continuity
        PreviousEntry previousEntry = findImmediatePredecessor(memberId, ledgerType, date, paymentId);
        BigDecimal premiumTotalBaseline = determinePremiumTotalBaseline(memberId, previousEntry);

        ComputedFields computed = computeFields(
            ledgerId, memberId, ledgerType, date,
            scheduledPayment, premium, actualPayment, remarks,
            previousEntry, premiumTotalBaseline
        );

        try {
            Connection con = Database.getConnection();
            String sql = """
                UPDATE form_data SET
                    date = ?, scheduled_payment = ?, actual_payment = ?, premium = ?, remarks = ?,
                    should_be_paid = ?, balance = ?, under_paid = ?, premium_total = ?, actual_payroll = ?,
                    updated_at = datetime('now')
                WHERE id = ? AND deleted_at IS NULL
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, date != null ? date.toString() : null);
            ps.setBigDecimal(2, scheduledPayment != null ? scheduledPayment : computed.scheduledPayment);
            ps.setBigDecimal(3, actualPayment);
            ps.setBigDecimal(4, premium);
            ps.setString(5, remarks);
            ps.setBigDecimal(6, computed.shouldBePaid);
            ps.setBigDecimal(7, computed.balance);
            ps.setBigDecimal(8, computed.underPaid);
            ps.setBigDecimal(9, computed.premiumTotal);
            ps.setBigDecimal(10, computed.actualPayroll);
            ps.setInt(11, paymentId);

            int rows = ps.executeUpdate();
            ps.close();
            con.close();

            if (rows > 0) {
                recomputeMemberChain(memberId, ledgerType, null);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Soft-delete a payment and recompute the continuous chain for that member/type.
     */
    public boolean softDeletePayment(int paymentId) {
        try {
            Connection con = Database.getConnection();
            String selectSql = """
                SELECT fd.member_id, fd.date, l.type
                FROM form_data fd
                INNER JOIN ledgers l ON fd.ledger_id = l.id
                WHERE fd.id = ? AND fd.deleted_at IS NULL
            """;
            PreparedStatement selectPs = con.prepareStatement(selectSql);
            selectPs.setInt(1, paymentId);
            ResultSet rs = selectPs.executeQuery();
            if (!rs.next()) {
                rs.close();
                selectPs.close();
                con.close();
                return false;
            }
            int memberId = rs.getInt("member_id");
            String ledgerType = rs.getString("type");
            LocalDate date = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(rs.getString("date"));
            rs.close();
            selectPs.close();

            PreparedStatement delPs = con.prepareStatement(
                "UPDATE form_data SET deleted_at = datetime('now'), updated_at = datetime('now') WHERE id = ?");
            delPs.setInt(1, paymentId);
            int rows = delPs.executeUpdate();
            delPs.close();
            con.close();

            if (rows > 0) {
                recomputeMemberChain(memberId, ledgerType, date);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Soft-delete a loan and recompute payment continuity (scheduled amounts / loan totals).
     */
    public boolean softDeleteLoan(int loanId) {
        try {
            Connection con = Database.getConnection();
            String selectSql = """
                SELECT l.member_id, l.date, led.type
                FROM loans l
                INNER JOIN ledgers led ON l.ledger_id = led.id
                WHERE l.id = ? AND l.deleted_at IS NULL
            """;
            PreparedStatement selectPs = con.prepareStatement(selectSql);
            selectPs.setInt(1, loanId);
            ResultSet rs = selectPs.executeQuery();
            if (!rs.next()) {
                rs.close();
                selectPs.close();
                con.close();
                return false;
            }
            int memberId = rs.getInt("member_id");
            String ledgerType = rs.getString("type");
            LocalDate date = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(rs.getString("date"));
            rs.close();
            selectPs.close();

            PreparedStatement delPs = con.prepareStatement(
                "UPDATE loans SET deleted_at = datetime('now'), updated_at = datetime('now') WHERE id = ?");
            delPs.setInt(1, loanId);
            int rows = delPs.executeUpdate();
            delPs.close();
            con.close();

            if (rows > 0) {
                recomputeMemberChain(memberId, ledgerType, date);
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private PreviousEntry findImmediatePredecessor(int memberId, String ledgerType, LocalDate date, int excludeId) {
        List<ChainRow> rows = loadChainRows(memberId, ledgerType);
        PreviousEntry prev = null;
        for (ChainRow row : rows) {
            if (row.id == excludeId) {
                break;
            }
            boolean beforeOrSame =
                date == null || row.date == null
                    || row.date.isBefore(date)
                    || (row.date.equals(date) && row.id < excludeId);
            if (beforeOrSame) {
                prev = toPreviousEntry(row);
            } else {
                break;
            }
        }
        return prev;
    }

    /**
     * Walk all active form_data rows for a member within a ledger type and re-derive
     * should_be_paid / balance / under_paid / premium_total / actual_payroll so the
     * continuous ledger stays consistent after create/edit/delete.
     *
     * @param fromDate when non-null, only rows on/after this date are rewritten (prior rows kept as anchors)
     */
    public void recomputeMemberChain(int memberId, String ledgerType, LocalDate fromDate) {
        List<ChainRow> rows = loadChainRows(memberId, ledgerType);
        if (rows.isEmpty()) {
            return;
        }

        PreviousEntry runningPrev = null;
        boolean started = (fromDate == null);

        for (ChainRow row : rows) {
            if (!started) {
                if (row.date != null && fromDate != null && row.date.isBefore(fromDate)) {
                    runningPrev = toPreviousEntry(row);
                    continue;
                }
                started = true;
            }

            BigDecimal premiumBaseline = determinePremiumTotalBaseline(memberId, runningPrev);
            // Pass null scheduledPayment so active loans re-drive the schedule after loan edits/deletes
            ComputedFields computed = computeFields(
                row.ledgerId, memberId, ledgerType, row.date,
                null, row.premium, row.actualPayment, row.remarks,
                runningPrev, premiumBaseline
            );

            persistComputed(row.id, computed, computed.scheduledPayment);
            runningPrev = toPreviousEntry(row, computed);
        }
    }

    private static class ChainRow {
        int id;
        int ledgerId;
        LocalDate date;
        BigDecimal scheduledPayment;
        BigDecimal premium;
        BigDecimal actualPayment;
        String remarks;
        BigDecimal balance;
        BigDecimal underPaid;
        BigDecimal premiumTotal;
    }

    private List<ChainRow> loadChainRows(int memberId, String ledgerType) {
        List<ChainRow> rows = new ArrayList<>();
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT fd.id, fd.ledger_id, fd.date, fd.scheduled_payment, fd.premium,
                       fd.actual_payment, fd.remarks, fd.balance, fd.under_paid, fd.premium_total
                FROM form_data fd
                INNER JOIN ledgers l ON fd.ledger_id = l.id
                WHERE l.type = ? AND fd.member_id = ?
                  AND fd.deleted_at IS NULL AND l.deleted_at IS NULL
                ORDER BY fd.date ASC, fd.id ASC
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, ledgerType);
            ps.setInt(2, memberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChainRow row = new ChainRow();
                row.id = rs.getInt("id");
                row.ledgerId = rs.getInt("ledger_id");
                row.date = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(rs.getString("date"));
                row.scheduledPayment = rs.getBigDecimal("scheduled_payment");
                row.premium = rs.getBigDecimal("premium");
                row.actualPayment = rs.getBigDecimal("actual_payment");
                row.remarks = rs.getString("remarks");
                row.balance = rs.getBigDecimal("balance");
                row.underPaid = rs.getBigDecimal("under_paid");
                row.premiumTotal = rs.getBigDecimal("premium_total");
                rows.add(row);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private PreviousEntry toPreviousEntry(ChainRow row) {
        PreviousEntry pe = new PreviousEntry();
        pe.id = row.id;
        pe.ledgerId = row.ledgerId;
        pe.date = row.date;
        pe.balance = row.balance;
        pe.underPaid = row.underPaid;
        pe.premiumTotal = row.premiumTotal;
        pe.premium = row.premium;
        pe.actualPayment = row.actualPayment;
        pe.scheduledPayment = row.scheduledPayment;
        pe.remarks = row.remarks;
        return pe;
    }

    private PreviousEntry toPreviousEntry(ChainRow row, ComputedFields computed) {
        PreviousEntry pe = toPreviousEntry(row);
        pe.balance = computed.balance;
        pe.underPaid = computed.underPaid;
        pe.premiumTotal = computed.premiumTotal;
        pe.shouldBePaid = computed.shouldBePaid;
        pe.actualPayroll = computed.actualPayroll;
        pe.scheduledPayment = computed.scheduledPayment;
        return pe;
    }

    private void persistComputed(int id, ComputedFields computed, BigDecimal scheduledPayment) {
        try {
            Connection con = Database.getConnection();
            String sql = """
                UPDATE form_data SET
                    scheduled_payment = ?, should_be_paid = ?, balance = ?, under_paid = ?,
                    premium_total = ?, actual_payroll = ?, updated_at = datetime('now')
                WHERE id = ?
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setBigDecimal(1, scheduledPayment);
            ps.setBigDecimal(2, computed.shouldBePaid);
            ps.setBigDecimal(3, computed.balance);
            ps.setBigDecimal(4, computed.underPaid);
            ps.setBigDecimal(5, computed.premiumTotal);
            ps.setBigDecimal(6, computed.actualPayroll);
            ps.setInt(7, id);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
