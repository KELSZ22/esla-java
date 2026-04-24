package services;

import com.kelsz.esla.Database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Loan Business Logic Service
 * Handles all loan creation and calculations according to ESLA business rules
 */
public class LoanService {

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
                FROM loans
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
     * Create a loan record
     *
     * @param ledgerId Ledger ID
     * @param memberId Member ID
     * @param formNumber Form number
     * @param date Loan date
     * @param principal Principal amount
     * @param serviceChargePercentage Service charge percentage (e.g., 5 for 5%)
     * @param noOfMonths Number of months/cutoffs
     * @param startDeductionDate Start deduction date
     * @param startDeductionOnLoanDate Whether to start deductions on loan date
     * @param remarks Remarks
     * @return The ID of the created loan record, or -1 if failed
     */
    public int createLoan(
            int ledgerId, int memberId, Integer formNumber, LocalDate date,
            BigDecimal principal, BigDecimal serviceChargePercentage, Integer noOfMonths,
            LocalDate startDeductionDate, Boolean startDeductionOnLoanDate, String remarks) {

        // Generate form number if not provided
        if (formNumber == null) {
            formNumber = generateFormNumber(ledgerId);
        }

        // Calculate service charge amount: principal * service_charge_balance (default 0.03)
        BigDecimal serviceCharge = calculateServiceCharge(principal);
        BigDecimal serviceChargeBalance = new BigDecimal("0.03"); // Default 3%

        // Calculate totals
        BigDecimal interest = calculateInterest(principal, noOfMonths, null);
        BigDecimal total = calculateTotal(principal, serviceCharge, interest);
        BigDecimal loanRange = calculateLoanRange(noOfMonths);
        BigDecimal cutoffsAmount = calculateCutoffsAmount(total, loanRange);

        try {
            Connection con = Database.getConnection();
            String sql = """
                INSERT INTO loans (
                    ledger_id, member_id, form_number, date,
                    principal, service_charge, interest, total,
                    cutoffs, cutoffs_amount, start_deduction_date,
                    start_deduction_on_loan_date,
                    service_charge_balance, interest_balance, remarks
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, ledgerId);
            ps.setInt(2, memberId);
            ps.setInt(3, formNumber);
            ps.setDate(4, date != null ? java.sql.Date.valueOf(date) : null);
            ps.setBigDecimal(5, principal);
            ps.setBigDecimal(6, serviceCharge); // Store calculated service charge amount
            ps.setBigDecimal(7, interest);
            ps.setBigDecimal(8, total);
            ps.setInt(9, noOfMonths);
            ps.setBigDecimal(10, cutoffsAmount);
            ps.setDate(11, startDeductionDate != null ? java.sql.Date.valueOf(startDeductionDate) : null);
            ps.setBoolean(12, startDeductionOnLoanDate != null ? startDeductionOnLoanDate : false);
            // Set balances equal to initial values for new loans
            ps.setBigDecimal(13, serviceChargeBalance); // Store service_charge_balance rate (0.03)
            ps.setBigDecimal(14, interest); // Store interest as interest_balance for new loans
            ps.setString(15, remarks);

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
     * Calculate service charge amount
     * Formula: principal * service_charge_balance (default 0.03)
     *
     * @param principal Principal amount
     * @return Calculated service charge amount
     */
    private BigDecimal calculateServiceCharge(BigDecimal principal) {
        if (principal == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal serviceChargeBalance = new BigDecimal("0.03"); // Default 3%
        return principal.multiply(serviceChargeBalance).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculate interest based on principal and number of cutoffs
     * Formula: principal * interest_balance * cutoffs
     * Default interest_balance: 0.03 (3%)
     *
     * @param principal Principal amount
     * @param noOfCutoffs Number of cutoffs
     * @param manualInterest Manual interest input (if principal or cutoffs is 0)
     * @return Calculated interest
     */
    private BigDecimal calculateInterest(BigDecimal principal, Integer noOfCutoffs, BigDecimal manualInterest) {
        if (principal == null || noOfCutoffs == null) {
            return manualInterest != null ? manualInterest : BigDecimal.ZERO;
        }
        
        if (principal.compareTo(BigDecimal.ZERO) > 0 && noOfCutoffs > 0) {
            BigDecimal interestBalance = new BigDecimal("0.03"); // Default 3%
            return principal.multiply(interestBalance).multiply(BigDecimal.valueOf(noOfCutoffs));
        }
        
        return manualInterest != null ? manualInterest : BigDecimal.ZERO;
    }

    /**
     * Calculate total loan amount
     *
     * @param principal Principal amount
     * @param serviceChargeBalance Service charge balance amount
     * @param interest Interest amount
     * @return Total amount
     */
    private BigDecimal calculateTotal(BigDecimal principal, BigDecimal serviceChargeBalance, BigDecimal interest) {
        BigDecimal total = BigDecimal.ZERO;
        if (principal != null) {
            total = total.add(principal);
        }
        if (serviceChargeBalance != null) {
            total = total.add(serviceChargeBalance);
        }
        if (interest != null) {
            total = total.add(interest);
        }
        return total;
    }

    /**
     * Calculate loan range: cutoffs * 2 (each cutoff = 2 payment periods)
     *
     * @param noOfCutoffs Number of cutoffs
     * @return Loan range
     */
    private BigDecimal calculateLoanRange(Integer noOfCutoffs) {
        if (noOfCutoffs == null || noOfCutoffs == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(noOfCutoffs * 2);
    }

    /**
     * Calculate cutoffs amount (per payment amount)
     * Formula: total / loan_range
     *
     * @param total Total amount
     * @param loanRange Loan range (cutoffs * 2)
     * @return Cutoffs amount
     */
    private BigDecimal calculateCutoffsAmount(BigDecimal total, BigDecimal loanRange) {
        if (total == null || loanRange == null || loanRange.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(loanRange, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Get effective deduction start date for a loan
     * Priority 1: start_deduction_date if set
     * Priority 2: loan_date if start_deduction_on_loan_date is true
     * Otherwise: null (deductions start after loan date)
     *
     * @param loanDate Loan date
     * @param startDeductionDate Start deduction date
     * @param startDeductionOnLoanDate Whether to start deductions on loan date
     * @return Effective deduction start date
     */
    public LocalDate getEffectiveDeductionStartDate(LocalDate loanDate, LocalDate startDeductionDate, Boolean startDeductionOnLoanDate) {
        if (startDeductionDate != null) {
            return startDeductionDate;
        }
        if (startDeductionOnLoanDate != null && startDeductionOnLoanDate && loanDate != null) {
            return loanDate;
        }
        return null;
    }

    /**
     * Check if prospective payment date should delay loan deduction
     * If loan has effective start date: returns true if prospective date < effective start
     * Otherwise: returns true if prospective date <= loan date
     *
     * @param prospectiveDate Prospective payment date
     * @param loanDate Loan date
     * @param effectiveStartDate Effective deduction start date
     * @return true if deduction should be delayed
     */
    public boolean shouldDelayDeductionStart(LocalDate prospectiveDate, LocalDate loanDate, LocalDate effectiveStartDate) {
        if (effectiveStartDate != null) {
            return prospectiveDate != null && prospectiveDate.isBefore(effectiveStartDate);
        }
        return prospectiveDate != null && loanDate != null && prospectiveDate.isBefore(loanDate);
    }

    /**
     * Calculate scheduled payment for a specific position
     * Iterates through all loans for a member across all same-type ledgers (ordered by ID) and sums up deductions for the given position
     *
     * @param memberId Member ID (to filter loans by member)
     * @param ledgerType Ledger type (to query across all same-type ledgers)
     * @param position Payment position (1-based)
     * @return Total scheduled payment amount for the position
     */
    public BigDecimal getScheduledPaymentForPosition(int ledgerId, int memberId, String ledgerType, int position) {
        Connection con = null;
        PreparedStatement loanPs = null;
        ResultSet loanRs = null;
        PreparedStatement paymentPs = null;
        ResultSet paymentRs = null;

        try {
            con = Database.getConnection();

            // Get all loans for the member across all same-type ledgers ordered by ID
            String loanSql = """
                SELECT l.id, l.form_number, l.date, l.start_deduction_date, l.start_deduction_on_loan_date,
                       l.principal, l.service_charge_balance, l.interest_balance, l.total, l.cutoffs
                FROM loans l
                INNER JOIN ledgers led ON l.ledger_id = led.id
                WHERE led.type = ? AND l.member_id = ? AND l.deleted_at IS NULL
                ORDER BY l.id ASC
            """;
            loanPs = con.prepareStatement(loanSql);
            loanPs.setString(1, ledgerType);
            loanPs.setInt(2, memberId);
            loanRs = loanPs.executeQuery();

            BigDecimal totalPayment = BigDecimal.ZERO;

            while (loanRs.next()) {
                LocalDate loanDate = loanRs.getDate("date") != null ? loanRs.getDate("date").toLocalDate() : null;
                LocalDate startDeductionDate = loanRs.getDate("start_deduction_date") != null ? loanRs.getDate("start_deduction_date").toLocalDate() : null;
                Boolean startDeductionOnLoanDate = loanRs.getBoolean("start_deduction_on_loan_date");
                BigDecimal total = loanRs.getBigDecimal("total");
                Integer cutoffs = loanRs.getInt("cutoffs");

                if (total == null || cutoffs == null || cutoffs == 0) {
                    continue;
                }

                // Step 2: Calculate loan's deduction range
                int range = cutoffs * 2;

                // Step 3: Calculate per-payment deduction amount
                BigDecimal amountPerCutoff = total.divide(BigDecimal.valueOf(range), 2, java.math.RoundingMode.HALF_UP);

                // Step 4: Determine loan's active payment range
                int rangeStart;

                if (startDeductionOnLoanDate != null && startDeductionOnLoanDate) {
                    // Starts at position 1 (immediate)
                    rangeStart = 1;
                } else if (startDeductionDate != null) {
                    // Starts at the payment position after start_deduction_date
                    String paymentSql = """
                        SELECT COUNT(*) as payment_count
                        FROM form_data fd
                        JOIN ledgers led ON fd.ledger_id = led.id
                        WHERE fd.ledger_id = ? AND fd.member_id = ? AND fd.deleted_at IS NULL AND fd.date <= ?
                    """;
                    paymentPs = con.prepareStatement(paymentSql);
                    paymentPs.setInt(1, ledgerId);
                    paymentPs.setInt(2, memberId);
                    paymentPs.setDate(3, java.sql.Date.valueOf(startDeductionDate));
                    paymentRs = paymentPs.executeQuery();

                    int paymentsBeforeDate = 0;
                    if (paymentRs.next()) {
                        paymentsBeforeDate = paymentRs.getInt("payment_count");
                    }
                    paymentRs.close();
                    paymentPs.close();
                    paymentRs = null;
                    paymentPs = null;

                    rangeStart = paymentsBeforeDate + 1;
                } else {
                    // Starts at position 2 (skips first payment after loan date)
                    String paymentSql = """
                        SELECT COUNT(*) as payment_count
                        FROM form_data fd
                        JOIN ledgers led ON fd.ledger_id = led.id
                        WHERE fd.ledger_id = ? AND fd.member_id = ? AND fd.deleted_at IS NULL AND fd.date < ?
                    """;
                    paymentPs = con.prepareStatement(paymentSql);
                    paymentPs.setInt(1, ledgerId);
                    paymentPs.setInt(2, memberId);
                    paymentPs.setDate(3, loanDate != null ? java.sql.Date.valueOf(loanDate) : null);
                    paymentRs = paymentPs.executeQuery();

                    int paymentsBeforeLoan = 0;
                    if (paymentRs.next()) {
                        paymentsBeforeLoan = paymentRs.getInt("payment_count");
                    }
                    paymentRs.close();
                    paymentPs.close();
                    paymentRs = null;
                    paymentPs = null;

                    rangeStart = paymentsBeforeLoan + 2;
                }

                int rangeEnd = rangeStart + range - 1;

                // Step 5: Check if current position is within loan range
                if (position >= rangeStart && position <= rangeEnd) {
                    // Step 6: Sum active loan deduction
                    totalPayment = totalPayment.add(amountPerCutoff);
                }
            }

            return totalPayment.setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (SQLException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        } finally {
            try {
                if (paymentRs != null) paymentRs.close();
                if (paymentPs != null) paymentPs.close();
                if (loanRs != null) loanRs.close();
                if (loanPs != null) loanPs.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get active loans at a specific position with detailed information
     * Queries loans for a member in the current ledger
     *
     * @param ledgerId Ledger ID
     * @param memberId Member ID (to filter loans by member)
     * @param ledgerType Ledger type
     * @param position Payment position (1-based)
     * @return List of active loan details for the position
     */
    public java.util.List<java.util.Map<String, Object>> getActiveLoansAtPosition(int ledgerId, int memberId, String ledgerType, int position) {
        java.util.List<java.util.Map<String, Object>> activeLoans = new java.util.ArrayList<>();
        Connection con = null;
        PreparedStatement loanPs = null;
        ResultSet loanRs = null;
        PreparedStatement paymentPs = null;
        ResultSet paymentRs = null;

        try {
            con = Database.getConnection();

            // Get all loans for the member across all same-type ledgers ordered by ID
            String loanSql = """
                SELECT l.id, l.form_number, l.date, l.start_deduction_date, l.start_deduction_on_loan_date,
                       l.principal, l.service_charge_balance, l.interest_balance, l.total, l.cutoffs
                FROM loans l
                INNER JOIN ledgers led ON l.ledger_id = led.id
                WHERE led.type = ? AND l.member_id = ? AND l.deleted_at IS NULL
                ORDER BY l.id ASC
            """;
            loanPs = con.prepareStatement(loanSql);
            loanPs.setString(1, ledgerType);
            loanPs.setInt(2, memberId);
            loanRs = loanPs.executeQuery();

            while (loanRs.next()) {
                int formNumber = loanRs.getInt("form_number");
                LocalDate loanDate = loanRs.getDate("date") != null ? loanRs.getDate("date").toLocalDate() : null;
                LocalDate startDeductionDate = loanRs.getDate("start_deduction_date") != null ? loanRs.getDate("start_deduction_date").toLocalDate() : null;
                Boolean startDeductionOnLoanDate = loanRs.getBoolean("start_deduction_on_loan_date");
                BigDecimal total = loanRs.getBigDecimal("total");
                Integer cutoffs = loanRs.getInt("cutoffs");

                if (total == null || cutoffs == null || cutoffs == 0) {
                    continue;
                }

                // Calculate loan's deduction range
                int range = cutoffs * 2;

                // Calculate per-payment deduction amount
                BigDecimal amountPerCutoff = total.divide(BigDecimal.valueOf(range), 2, java.math.RoundingMode.HALF_UP);

                // Determine loan's active payment range
                int rangeStart;

                if (startDeductionOnLoanDate != null && startDeductionOnLoanDate) {
                    // Starts at position 1 (immediate)
                    rangeStart = 1;
                } else if (startDeductionDate != null) {
                    // Starts at the payment position after start_deduction_date
                    String paymentSql = """
                        SELECT COUNT(*) as payment_count
                        FROM form_data fd
                        JOIN ledgers led ON fd.ledger_id = led.id
                        WHERE fd.ledger_id = ? AND fd.member_id = ? AND fd.deleted_at IS NULL AND fd.date <= ?
                    """;
                    paymentPs = con.prepareStatement(paymentSql);
                    paymentPs.setInt(1, ledgerId);
                    paymentPs.setInt(2, memberId);
                    paymentPs.setDate(3, java.sql.Date.valueOf(startDeductionDate));
                    paymentRs = paymentPs.executeQuery();

                    int paymentsBeforeDate = 0;
                    if (paymentRs.next()) {
                        paymentsBeforeDate = paymentRs.getInt("payment_count");
                    }
                    paymentRs.close();
                    paymentPs.close();
                    paymentRs = null;
                    paymentPs = null;

                    rangeStart = paymentsBeforeDate + 1;
                } else {
                    // Starts at position 2 (skips first payment after loan date)
                    String paymentSql = """
                        SELECT COUNT(*) as payment_count
                        FROM form_data fd
                        JOIN ledgers led ON fd.ledger_id = led.id
                        WHERE fd.ledger_id = ? AND fd.member_id = ? AND fd.deleted_at IS NULL AND fd.date < ?
                    """;
                    paymentPs = con.prepareStatement(paymentSql);
                    paymentPs.setInt(1, ledgerId);
                    paymentPs.setInt(2, memberId);
                    paymentPs.setDate(3, loanDate != null ? java.sql.Date.valueOf(loanDate) : null);
                    paymentRs = paymentPs.executeQuery();

                    int paymentsBeforeLoan = 0;
                    if (paymentRs.next()) {
                        paymentsBeforeLoan = paymentRs.getInt("payment_count");
                    }
                    paymentRs.close();
                    paymentPs.close();
                    paymentRs = null;
                    paymentPs = null;

                    rangeStart = paymentsBeforeLoan + 2;
                }

                int rangeEnd = rangeStart + range - 1;

                // Check if current position is within loan range
                if (position >= rangeStart && position <= rangeEnd) {
                    int paymentsCompleted = position - rangeStart + 1;
                    BigDecimal remainingBalance = total.subtract(amountPerCutoff.multiply(BigDecimal.valueOf(paymentsCompleted)));
                    boolean isFirstDeduction = (position == rangeStart);

                    java.util.Map<String, Object> loanInfo = new java.util.HashMap<>();
                    loanInfo.put("form_number", formNumber);
                    loanInfo.put("amount_per_cutoff", amountPerCutoff);
                    loanInfo.put("total", total);
                    loanInfo.put("remaining_balance", remainingBalance);
                    loanInfo.put("is_first_deduction", isFirstDeduction);
                    activeLoans.add(loanInfo);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (paymentRs != null) paymentRs.close();
                if (paymentPs != null) paymentPs.close();
                if (loanRs != null) loanRs.close();
                if (loanPs != null) loanPs.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return activeLoans;
    }

    /**
     * Get loans on a specific date across same-type ledgers
     * Used in FormDataService to add loan totals to payment calculations
     *
     * @param ledgerType Ledger type
     * @param date Date to filter loans
     * @return List of loans matching the date
     */
    public java.util.List<java.util.Map<String, Object>> getLoansOnDate(String ledgerType, LocalDate date) {
        java.util.List<java.util.Map<String, Object>> loans = new java.util.ArrayList<>();

        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT l.id, l.form_number, l.date, l.principal, l.service_charge, l.interest, l.total,
                       l.cutoffs, l.cutoffs_amount, l.member_id, m.name as member_name
                FROM loans l
                JOIN ledgers led ON l.ledger_id = led.id
                JOIN members m ON l.member_id = m.id
                WHERE led.type = ? AND l.date = ? AND l.deleted_at IS NULL
                ORDER BY l.id ASC
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, ledgerType);
            ps.setDate(2, date != null ? java.sql.Date.valueOf(date) : null);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                java.util.Map<String, Object> loanInfo = new java.util.HashMap<>();
                loanInfo.put("id", rs.getInt("id"));
                loanInfo.put("form_number", rs.getInt("form_number"));
                loanInfo.put("date", rs.getDate("date"));
                loanInfo.put("principal", rs.getBigDecimal("principal"));
                loanInfo.put("service_charge", rs.getBigDecimal("service_charge"));
                loanInfo.put("interest", rs.getBigDecimal("interest"));
                loanInfo.put("total", rs.getBigDecimal("total"));
                loanInfo.put("cutoffs", rs.getInt("cutoffs"));
                loanInfo.put("cutoffs_amount", rs.getBigDecimal("cutoffs_amount"));
                loanInfo.put("member_id", rs.getInt("member_id"));
                loanInfo.put("member_name", rs.getString("member_name"));
                loans.add(loanInfo);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return loans;
    }

    /**
     * Get scheduled payment for the next position
     * Counts existing payment entries in the current ledger and calls getScheduledPaymentForPosition
     *
     * @param ledgerId Ledger ID
     * @param memberId Member ID (to filter loans by member)
     * @param ledgerType Ledger type
     * @return Scheduled payment for the next position
     */
    public BigDecimal getScheduledPaymentForNextPosition(int ledgerId, int memberId, String ledgerType) {
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT COUNT(*) as payment_count
                FROM form_data fd
                JOIN ledgers led ON fd.ledger_id = led.id
                WHERE fd.ledger_id = ? AND fd.deleted_at IS NULL
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, ledgerId);
            ResultSet rs = ps.executeQuery();

            int paymentCount = 0;
            if (rs.next()) {
                paymentCount = rs.getInt("payment_count");
            }

            rs.close();
            ps.close();
            con.close();

            return getScheduledPaymentForPosition(ledgerId, memberId, ledgerType, paymentCount + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get scheduled payment considering prospective date
     * Calculates scheduled payment for the prospective date based on actual loan deduction date ranges
     * Queries loans for a member in the current ledger
     *
     * @param ledgerId Ledger ID
     * @param memberId Member ID (to filter loans by member)
     * @param ledgerType Ledger type
     * @param prospectiveDate Prospective payment date
     * @return Scheduled payment amount
     */
    public BigDecimal getScheduledPaymentWithDate(int ledgerId, int memberId, String ledgerType, LocalDate prospectiveDate) {
        Connection con = null;
        PreparedStatement loanPs = null;
        ResultSet loanRs = null;

        try {
            con = Database.getConnection();
            if (con == null) {
                return BigDecimal.ZERO;
            }

            // Get all loans for the member across all same-type ledgers ordered by ID
            String loanSql = """
                SELECT l.id, l.form_number, l.date, l.start_deduction_date, l.start_deduction_on_loan_date,
                       l.principal, l.service_charge_balance, l.interest_balance, l.total, l.cutoffs
                FROM loans l
                INNER JOIN ledgers led ON l.ledger_id = led.id
                WHERE led.type = ? AND l.member_id = ? AND l.deleted_at IS NULL
                ORDER BY l.id ASC
            """;
            loanPs = con.prepareStatement(loanSql);
            loanPs.setString(1, ledgerType);
            loanPs.setInt(2, memberId);
            loanRs = loanPs.executeQuery();

            BigDecimal totalPayment = BigDecimal.ZERO;

            while (loanRs.next()) {
                int loanId = loanRs.getInt("id");
                LocalDate loanDate = loanRs.getDate("date") != null ? loanRs.getDate("date").toLocalDate() : null;
                LocalDate startDeductionDate = loanRs.getDate("start_deduction_date") != null ? loanRs.getDate("start_deduction_date").toLocalDate() : null;
                Boolean startDeductionOnLoanDate = loanRs.getBoolean("start_deduction_on_loan_date");
                BigDecimal total = loanRs.getBigDecimal("total");
                Integer cutoffs = loanRs.getInt("cutoffs");

                if (total == null || cutoffs == null || cutoffs == 0) {
                    continue;
                }

                // Check if this loan is fully paid across all ledgers
                if (isLoanFullyPaid(loanId, total)) {
                    continue;
                }

                // Calculate loan's deduction range in terms of payment periods
                int range = cutoffs * 2;

                // Calculate per-payment deduction amount
                BigDecimal amountPerCutoff = total.divide(BigDecimal.valueOf(range), 2, java.math.RoundingMode.HALF_UP);

                // Determine the actual date when deductions should start
                LocalDate effectiveStartDate = getEffectiveDeductionStartDate(loanDate, startDeductionDate, startDeductionOnLoanDate);

                // If no effective start date, skip this loan
                if (effectiveStartDate == null) {
                    continue;
                }

                // If prospective date is before effective start date, skip this loan
                if (prospectiveDate != null && prospectiveDate.isBefore(effectiveStartDate)) {
                    continue;
                }

                // Count how many payment periods have occurred from effectiveStartDate to prospectiveDate
                // Cutoff dates are 15th and last day of each month
                LocalDate currentDate = effectiveStartDate;
                int paymentPeriod = 0;
                boolean loanIsActive = false;

                while (!currentDate.isAfter(prospectiveDate)) {
                    int dayOfMonth = currentDate.getDayOfMonth();
                    int lastDayOfMonth = currentDate.lengthOfMonth();
                    
                    // Check if currentDate is a cutoff date (15th or last day)
                    if (dayOfMonth == 15 || dayOfMonth == lastDayOfMonth) {
                        paymentPeriod++;
                        
                        // Check if this payment period is within the loan's range
                        if (paymentPeriod >= 1 && paymentPeriod <= range) {
                            // Check if prospectiveDate matches this cutoff date
                            if (currentDate.equals(prospectiveDate)) {
                                loanIsActive = true;
                                break;
                            }
                        }
                    }
                    
                    currentDate = currentDate.plusDays(1);
                }

                // If the prospective date falls within the loan's active payment periods, add the amount
                if (loanIsActive) {
                    totalPayment = totalPayment.add(amountPerCutoff);
                }
            }

            return totalPayment.setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (SQLException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        } finally {
            try {
                if (loanRs != null) loanRs.close();
                if (loanPs != null) loanPs.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if a loan is fully paid across all ledgers
     * @param loanId The loan ID to check
     * @param total The total loan amount
     * @return true if the loan is fully paid, false otherwise
     */
    private boolean isLoanFullyPaid(int loanId, BigDecimal total) {
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT SUM(fd.scheduled_payment) as total_paid
                FROM form_data fd
                WHERE fd.scheduled_payment IS NOT NULL
                AND fd.scheduled_payment > 0
                AND fd.date IN (
                    SELECT l.date
                    FROM loans l
                    WHERE l.id = ?
                )
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, loanId);
            ResultSet rs = ps.executeQuery();

            BigDecimal totalPaid = BigDecimal.ZERO;
            if (rs.next()) {
                totalPaid = rs.getBigDecimal("total_paid");
                if (totalPaid == null) {
                    totalPaid = BigDecimal.ZERO;
                }
            }

            rs.close();
            ps.close();
            con.close();

            // Consider loan fully paid if total paid >= total loan amount
            return totalPaid.compareTo(total) >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
