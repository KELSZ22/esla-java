package services;

import com.kelsz.esla.Database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Form Data Service
 * Handles prospective payment calculations for payment form prediction
 */
public class FormDataService {

    /**
     * Represents prospective payment calculation result
     */
    public static class ProspectivePayment {
        public BigDecimal scheduledPayment;
        public BigDecimal shouldBePaid;
    }

    /**
     * Get prospective payments for a specific date
     * Calculates scheduled payment and should_be_paid considering:
     * - Position for the prospective date (within the current ledger)
     * - Loans for the member that may start on or before that date (within the current ledger)
     * - Previous underpaid amounts for the member within the current ledger
     *
     * @param ledgerId Ledger ID
     * @param memberId Member ID
     * @param ledgerType Ledger type
     * @param date Prospective payment date
     * @return ProspectivePayment with scheduled_payment and should_be_paid rounded to 2 decimals
     */
    public ProspectivePayment getProspectivePaymentsForDate(
            int ledgerId, int memberId, String ledgerType, LocalDate date) {

        ProspectivePayment result = new ProspectivePayment();
        Connection con = null;
        PreparedStatement previousPs = null;
        ResultSet previousRs = null;

        try {
            System.out.println("DEBUG FormDataService: ledgerId=" + ledgerId + ", memberId=" + memberId + ", ledgerType=" + ledgerType + ", date=" + date);
            // 1. Get scheduled payment for next position considering the prospective date
            // Use LoanService to get scheduled payment considering prospective date for the member
            LoanService loanService = new LoanService();
            BigDecimal scheduledPayment = loanService.getScheduledPaymentWithDate(ledgerId, memberId, ledgerType, date);
            System.out.println("DEBUG FormDataService: scheduledPayment from LoanService=" + scheduledPayment);
            result.scheduledPayment = scheduledPayment.setScale(2, java.math.RoundingMode.HALF_UP);

            // 2. Find previous underpaid amount for the member across all same-type ledgers (before the prospective date)
            // This is member-specific and ledger-type-specific for continuous payment tracking
            con = Database.getConnection();
            String previousSql = """
                SELECT fd.under_paid
                FROM form_data fd
                INNER JOIN ledgers l ON fd.ledger_id = l.id
                WHERE l.type = ? AND fd.member_id = ? AND fd.date < ? AND fd.deleted_at IS NULL
                ORDER BY fd.date DESC
                LIMIT 1
            """;
            previousPs = con.prepareStatement(previousSql);
            previousPs.setString(1, ledgerType);
            previousPs.setInt(2, memberId);
            previousPs.setString(3, date != null ? date.toString() : null);
            previousRs = previousPs.executeQuery();

            BigDecimal previousUnderPaid = BigDecimal.ZERO;
            if (previousRs.next()) {
                previousUnderPaid = previousRs.getBigDecimal("under_paid");
                if (previousUnderPaid == null) {
                    previousUnderPaid = BigDecimal.ZERO;
                }
                System.out.println("DEBUG FormDataService: Found previous underpaid=" + previousUnderPaid + " in ledgerId=" + ledgerId);
            } else {
                System.out.println("DEBUG FormDataService: No previous underpaid found in ledgerId=" + ledgerId);
            }

            // 3. Calculate should_be_paid = previous_under_paid + scheduled_payment
            result.shouldBePaid = previousUnderPaid.add(result.scheduledPayment)
                .setScale(2, java.math.RoundingMode.HALF_UP);
            System.out.println("DEBUG FormDataService: Final shouldBePaid=" + result.shouldBePaid);
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Return zero values on error
            result.scheduledPayment = BigDecimal.ZERO.setScale(2);
            result.shouldBePaid = BigDecimal.ZERO.setScale(2);
        } finally {
            try {
                if (previousRs != null) previousRs.close();
                if (previousPs != null) previousPs.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }

    /**
     * Get member premium amount
     *
     * @param memberId Member ID
     * @return Premium amount or null if not found
     */
    public BigDecimal getMemberPremium(int memberId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = Database.getConnection();
            String sql = "SELECT premium FROM members WHERE id = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, memberId);
            rs = ps.executeQuery();
            
            BigDecimal premium = null;
            if (rs.next()) {
                premium = rs.getBigDecimal("premium");
            }
            
            return premium;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
