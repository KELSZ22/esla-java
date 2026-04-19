package services;

import com.kelsz.esla.Database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Payment Business Logic Service
 * Handles all payment calculations and record creation according to ESLA business rules
 */
public class PaymentService {

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
     * 1. Find Previous Entry (lines 223-229)
     * Queries all same-type ledgers (ledgers with matching type field)
     * Finds the most recent form_data entry for this member across all same-type ledgers
     * This establishes the baseline for calculations
     *
     * @param ledgerId Current ledger ID
     * @param memberId Member ID
     * @param ledgerType Ledger type to filter by
     * @return PreviousEntry or null if no previous entry exists
     */
    public PreviousEntry findPreviousEntry(int ledgerId, int memberId, String ledgerType) {
        PreviousEntry previousEntry = null;
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT fd.id, fd.ledger_id, fd.member_id, fd.form_number, fd.is_loan, fd.date,
                       fd.should_be_paid, fd.actual_payment, fd.balance, fd.under_paid,
                       fd.scheduled_payment, fd.premium_total, fd.premium, fd.actual_payroll, fd.remarks
                FROM form_data fd
                INNER JOIN ledgers l ON fd.ledger_id = l.id
                WHERE l.type = ? AND fd.member_id = ?
                ORDER BY fd.date DESC
                LIMIT 1
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, ledgerType);
            ps.setInt(2, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                previousEntry = new PreviousEntry();
                previousEntry.id = rs.getInt("id");
                previousEntry.ledgerId = rs.getInt("ledger_id");
                previousEntry.memberId = rs.getInt("member_id");
                previousEntry.formNumber = rs.getObject("form_number", Integer.class);
                previousEntry.isLoan = rs.getObject("is_loan", Boolean.class);
                previousEntry.date = rs.getDate("date").toLocalDate();
                previousEntry.shouldBePaid = rs.getBigDecimal("should_be_paid");
                previousEntry.actualPayment = rs.getBigDecimal("actual_payment");
                previousEntry.balance = rs.getBigDecimal("balance");
                previousEntry.underPaid = rs.getBigDecimal("under_paid");
                previousEntry.scheduledPayment = rs.getBigDecimal("scheduled_payment");
                previousEntry.premiumTotal = rs.getBigDecimal("premium_total");
                previousEntry.premium = rs.getBigDecimal("premium");
                previousEntry.actualPayroll = rs.getBigDecimal("actual_payroll");
                previousEntry.remarks = rs.getString("remarks");
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
     * 2. Determine Premium Total Baseline (lines 231-240)
     * If no previous entry exists in same-type ledgers:
     * Searches globally for the last entry with premium_total > 0
     * Uses that premium_total as baseline
     * This ensures premium totals carry over when starting a new ledger type
     *
     * @param memberId Member ID
     * @param previousEntry The previous entry (may be null)
     * @return Premium total baseline or null if none exists
     */
    public BigDecimal determinePremiumTotalBaseline(int memberId, PreviousEntry previousEntry) {
        // If previous entry exists, use its premium_total
        if (previousEntry != null && previousEntry.premiumTotal != null) {
            return previousEntry.premiumTotal;
        }

        // Otherwise, search globally for last entry with premium_total > 0
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT premium_total
                FROM form_data
                WHERE member_id = ? AND premium_total IS NOT NULL AND premium_total > 0
                ORDER BY date DESC
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
     * 3. Compute Fields (line 242)
     * Calls computedFields() with validated form data, previous entry, premium total baseline,
     * ledger and member info, current form data (null for create)
     *
     * @param ledgerId Ledger ID
     * @param memberId Member ID
     * @param ledgerType Ledger type
     * @param date Payment date
     * @param scheduledPayment Scheduled payment amount
     * @param premium Premium amount
     * @param actualPayment Actual payment amount
     * @param remarks Remarks
     * @param previousEntry Previous entry (may be null)
     * @param premiumTotalBaseline Premium total baseline (may be null)
     * @return ComputedFields object with all calculated values
     */
    public ComputedFields computeFields(
            int ledgerId, int memberId, String ledgerType, LocalDate date,
            BigDecimal scheduledPayment, BigDecimal premium, BigDecimal actualPayment, String remarks,
            PreviousEntry previousEntry, BigDecimal premiumTotalBaseline) {

        ComputedFields computed = new ComputedFields();

        // 4. Computed Fields Logic (lines 412-476)
        
        // Get loan information for this member and date
        LoanInfo loanInfo = getLoanInfo(ledgerId, memberId, ledgerType, date);

        // Interest Calculation: principal * interest_balance * cutoff_count (if loan)
        if (loanInfo != null && loanInfo.principal != null && loanInfo.interestBalance != null && loanInfo.cutoffs != null) {
            computed.interest = loanInfo.principal
                .multiply(loanInfo.interestBalance)
                .multiply(BigDecimal.valueOf(loanInfo.cutoffs));
        } else {
            computed.interest = BigDecimal.ZERO;
        }

        // Service Charge: principal * service_charge_balance (if loan)
        if (loanInfo != null && loanInfo.principal != null && loanInfo.serviceChargeBalance != null) {
            computed.serviceCharge = loanInfo.principal.multiply(loanInfo.serviceChargeBalance);
        } else {
            computed.serviceCharge = BigDecimal.ZERO;
        }

        // Total: Sum of principal + interest + service charge, OR fetches total from loan table if a loan exists on that date
        if (loanInfo != null && loanInfo.total != null) {
            computed.total = loanInfo.total;
        } else if (loanInfo != null) {
            computed.total = loanInfo.principal != null ? loanInfo.principal : BigDecimal.ZERO
                .add(computed.interest)
                .add(computed.serviceCharge);
        } else {
            computed.total = BigDecimal.ZERO;
        }

        // Scheduled Payment: Fetches from LoanService based on position and date
        // For now, use the provided scheduledPayment value
        computed.scheduledPayment = scheduledPayment != null ? scheduledPayment : BigDecimal.ZERO;

        // Should Be Paid: Previous under_paid + scheduled_payment
        BigDecimal previousUnderPaid = (previousEntry != null && previousEntry.underPaid != null) 
            ? previousEntry.underPaid 
            : BigDecimal.ZERO;
        computed.shouldBePaid = previousUnderPaid.add(computed.scheduledPayment);

        // Balance: Previous balance - actual_payment + total
        BigDecimal previousBalance = (previousEntry != null && previousEntry.balance != null) 
            ? previousEntry.balance 
            : BigDecimal.ZERO;
        computed.balance = previousBalance
            .subtract(actualPayment != null ? actualPayment : BigDecimal.ZERO)
            .add(computed.total);

        // Under Paid: should_be_paid - actual_payment
        computed.underPaid = computed.shouldBePaid.subtract(
            actualPayment != null ? actualPayment : BigDecimal.ZERO
        );

        // Premium Total: Previous premium_total + current premium
        BigDecimal previousPremiumTotal = premiumTotalBaseline != null ? premiumTotalBaseline : BigDecimal.ZERO;
        computed.premiumTotal = previousPremiumTotal.add(premium != null ? premium : BigDecimal.ZERO);

        // Actual Payroll: actual_payment + premium
        computed.actualPayroll = (actualPayment != null ? actualPayment : BigDecimal.ZERO)
            .add(premium != null ? premium : BigDecimal.ZERO);

        return computed;
    }

    /**
     * 5. Create Record (lines 244-254)
     * Stores ledger_id, member_id, is_loan, form_number, date
     * Stores user inputs: scheduled_payment, premium, actual_payment, remarks
     * Stores computed fields: should_be_paid, balance, under_paid, premium_total, actual_payroll
     *
     * @param ledgerId Ledger ID
     * @param memberId Member ID
     * @param ledgerType Ledger type
     * @param date Payment date
     * @param scheduledPayment Scheduled payment amount
     * @param premium Premium amount
     * @param actualPayment Actual payment amount
     * @param remarks Remarks
     * @param computed Computed fields
     * @return The ID of the created record, or -1 if failed
     */
    public int createPaymentRecord(
            int ledgerId, int memberId, String ledgerType, LocalDate date,
            BigDecimal scheduledPayment, BigDecimal premium, BigDecimal actualPayment, String remarks,
            ComputedFields computed) {

        // Generate form number
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
            ps.setBoolean(4, false); // is_loan
            ps.setDate(5, date != null ? java.sql.Date.valueOf(date) : null);
            ps.setBigDecimal(6, scheduledPayment);
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

    /**
     * 6. Loan Integration (lines 482-506)
     * Checks if a loan exists on the same date across same-type ledgers
     * If loan exists, uses loan's total instead of computed total
     * This ensures loan amounts are reflected in payment calculations
     *
     * @param ledgerId Ledger ID
     * @param memberId Member ID
     * @param ledgerType Ledger type
     * @param date Date to check
     * @return LoanInfo or null if no loan exists
     */
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
                loanInfo.date = rs.getDate("date").toLocalDate();
                loanInfo.startDeductionOnLoanDate = rs.getObject("start_deduction_on_loan_date", Boolean.class);
                loanInfo.startDeductionDate = rs.getDate("start_deduction_date") != null 
                    ? rs.getDate("start_deduction_date").toLocalDate() 
                    : null;
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

    /**
     * 7. Position Tracking (lines 511-529)
     * Calculates 1-based global position across all same-type ledgers
     * Used to determine which loans are active at that payment position
     * Loans deduct from scheduled payments based on their cutoff configuration
     *
     * @param ledgerId Ledger ID
     * @param memberId Member ID
     * @param ledgerType Ledger type
     * @param date Date to calculate position for
     * @return 1-based global position
     */
    public int calculatePosition(int ledgerId, int memberId, String ledgerType, LocalDate date) {
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT COUNT(*) + 1 as position
                FROM form_data fd
                INNER JOIN ledgers l ON fd.ledger_id = l.id
                WHERE l.type = ? AND fd.member_id = ? AND fd.date < ?
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

    /**
     * Generate the next form number for a ledger
     *
     * @param ledgerId Ledger ID
     * @return Next form number
     */
    private int generateFormNumber(int ledgerId) {
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT COALESCE(MAX(form_number), 0) + 1 as next_form_number
                FROM form_data
                WHERE ledger_id = ?
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
     * Main method to process payment creation
     * Orchestrates all the business logic steps
     *
     * @param ledgerId Ledger ID
     * @param memberId Member ID
     * @param ledgerType Ledger type
     * @param date Payment date
     * @param scheduledPayment Scheduled payment amount
     * @param premium Premium amount
     * @param actualPayment Actual payment amount
     * @param remarks Remarks
     * @return The ID of the created record, or -1 if failed
     */
    public int processPayment(
            int ledgerId, int memberId, String ledgerType, LocalDate date,
            BigDecimal scheduledPayment, BigDecimal premium, BigDecimal actualPayment, String remarks) {

        // Step 1: Find Previous Entry
        PreviousEntry previousEntry = findPreviousEntry(ledgerId, memberId, ledgerType);

        // Step 2: Determine Premium Total Baseline
        BigDecimal premiumTotalBaseline = determinePremiumTotalBaseline(memberId, previousEntry);

        // Step 3: Compute Fields
        ComputedFields computed = computeFields(
            ledgerId, memberId, ledgerType, date,
            scheduledPayment, premium, actualPayment, remarks,
            previousEntry, premiumTotalBaseline
        );

        // Step 4: Create Record
        int recordId = createPaymentRecord(
            ledgerId, memberId, ledgerType, date,
            scheduledPayment, premium, actualPayment, remarks,
            computed
        );

        return recordId;
    }
}
