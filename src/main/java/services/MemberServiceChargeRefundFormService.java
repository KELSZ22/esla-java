package services;

import com.kelsz.esla.Database;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for Member Service Charge Refund Forms
 * Handles calculations for service charge refunds based on loan data
 */
public class MemberServiceChargeRefundFormService {



    /**
     * Create refund form entries from loans for a member in a date range
     * This is the equivalent of bulkInsert/instantBulkInsert in PHP
     *
     * @param memberId Member ID
     * @param dateFrom Refund period start date
     * @param dateTo Refund period end date
     * @param mscrRefundId The member_service_charge_refunds ID
     * @return Number of forms created
     */
    public int bulkInsertFromLoans(int memberId, LocalDate dateFrom, LocalDate dateTo, int mscrRefundId) {
        try (Connection con = Database.getConnection()) {
            return bulkInsertFromLoans(con, memberId, dateFrom, dateTo, mscrRefundId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Overloaded version that accepts a connection
     */
    public int bulkInsertFromLoans(Connection con, int memberId, LocalDate dateFrom, LocalDate dateTo, int mscrRefundId) {
        try {

            // Pre-fetch all form_data dates for this member to calculate positions
            java.util.Map<String, java.util.List<LocalDate>> ledgerTypeFormDates = new java.util.HashMap<>();
            String allPaymentsSql = """
                SELECT led.type as ledger_type, fd.date
                FROM form_data fd
                JOIN ledgers led ON fd.ledger_id = led.id
                WHERE fd.member_id = ? AND fd.deleted_at IS NULL
                ORDER BY fd.date ASC
            """;
            PreparedStatement allPs = con.prepareStatement(allPaymentsSql);
            allPs.setInt(1, memberId);
            ResultSet allRs = allPs.executeQuery();
            while (allRs.next()) {
                String type = allRs.getString("ledger_type");
                String fdDateStr = allRs.getString("date");
                LocalDate parsedDate = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(fdDateStr);
                if (parsedDate != null) {
                    ledgerTypeFormDates.computeIfAbsent(type, k -> new java.util.ArrayList<>()).add(parsedDate);
                }
            }
            allRs.close();
            allPs.close();

            // Get all loans for the member
            String loanSql = """
                SELECT l.id, l.ledger_id, led.type as ledger_type, l.form_number, l.date, 
                       l.start_deduction_date, l.start_deduction_on_loan_date,
                       l.principal, l.interest, l.service_charge, l.total, l.cutoffs, l.remarks
                FROM loans l
                JOIN ledgers led ON l.ledger_id = led.id
                WHERE l.member_id = ? AND l.deleted_at IS NULL
                ORDER BY l.id ASC
            """;
            PreparedStatement loanPs = con.prepareStatement(loanSql);
            loanPs.setInt(1, memberId);
            ResultSet loanRs = loanPs.executeQuery();

            // Prepare insert statement for refund forms
            String insertSql = """
                INSERT INTO member_service_charge_refund_forms (
                    mscr_refund_id, form_number, date_loan, principal, interest, service_charge,
                    total, no_of_months, collected_interest, total_interest, refund_60, refund_40,
                    remarks, has_balance, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))
            """;
            PreparedStatement insertPs = con.prepareStatement(insertSql);

            int count = 0;
            while (loanRs.next()) {
                String ledgerType = loanRs.getString("ledger_type");
                int formNumber = loanRs.getInt("form_number");
                
                String dateStr = loanRs.getString("date");
                LocalDate loanDate = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(dateStr);

                String sddStr = loanRs.getString("start_deduction_date");
                LocalDate startDeductionDate = com.kelsz.esla.util.DateUtils.parseLocalDateSafely(sddStr);
                
                Boolean startDeductionOnLoanDate = loanRs.getBoolean("start_deduction_on_loan_date");
                BigDecimal principal = loanRs.getBigDecimal("principal");
                BigDecimal interest = loanRs.getBigDecimal("interest");
                BigDecimal serviceCharge = loanRs.getBigDecimal("service_charge");
                BigDecimal total = loanRs.getBigDecimal("total");
                Integer cutoffs = loanRs.getInt("cutoffs");
                String loanRemarks = loanRs.getString("remarks");

                // Calculate no_of_months from cutoffs (floored to at least 0.5)
                double noOfMonths = Math.max(0.5, cutoffs != null ? cutoffs.doubleValue() : 0.5);

                // Calculate total if missing
                if (total == null) {
                    total = BigDecimal.ZERO;
                    if (principal != null) total = total.add(principal);
                    if (interest != null) total = total.add(interest);
                    if (serviceCharge != null) total = total.add(serviceCharge);
                }

                // Calculate collected interest based on actual ledger form_data entries
                double collectedInterest = 0.0;
                if (cutoffs != null && cutoffs > 0 && dateFrom != null && dateTo != null) {
                    java.util.List<LocalDate> dates = ledgerTypeFormDates.getOrDefault(ledgerType, new ArrayList<>());
                    
                    int rangeStart = -1;
                    if (startDeductionOnLoanDate != null && startDeductionOnLoanDate) {
                        rangeStart = 1;
                    } else if (startDeductionDate != null) {
                        int countBeforeOrOn = 0;
                        for (LocalDate d : dates) {
                            if (!d.isAfter(startDeductionDate)) countBeforeOrOn++;
                        }
                        rangeStart = countBeforeOrOn + 1;
                    } else {
                        int countBefore = 0;
                        if (loanDate != null) {
                            for (LocalDate d : dates) {
                                if (d.isBefore(loanDate)) countBefore++;
                            }
                        }
                        rangeStart = countBefore + 2;
                    }
                    
                    int rangeEnd = rangeStart + (cutoffs * 2) - 1;
                    int collectedCutoffs = 0;
                    
                    for (int i = 0; i < dates.size(); i++) {
                        int pos = i + 1;
                        if (pos >= rangeStart && pos <= rangeEnd) {
                            LocalDate d = dates.get(i);
                            if (!d.isBefore(dateFrom) && !d.isAfter(dateTo)) {
                                collectedCutoffs++;
                            }
                        }
                    }
                    collectedInterest = collectedCutoffs * 0.5;
                }

                // Calculate total interest (pro-rata based on collected interest)
                BigDecimal totalInterest = BigDecimal.ZERO;
                if (noOfMonths > 0 && interest != null) {
                    totalInterest = interest.divide(BigDecimal.valueOf(noOfMonths), 2, RoundingMode.HALF_UP)
                                          .multiply(BigDecimal.valueOf(collectedInterest));
                }

                // Calculate refund splits (60/40)
                BigDecimal refund60 = totalInterest.multiply(new BigDecimal("0.60")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal refund40 = totalInterest.multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP);

                // Calculate has_balance and remarks
                double remainingMonths = Math.max(0.0, noOfMonths - collectedInterest);
                boolean hasBalance = remainingMonths > 0.0;
                
                StringBuilder remarks = new StringBuilder();
                if (loanRemarks != null && !loanRemarks.trim().isEmpty()) {
                    remarks.append(loanRemarks).append(" ");
                }
                if (hasBalance) {
                    remarks.append("Balance ").append(String.format("%.1f", remainingMonths));
                } else {
                    remarks.append("Fully collected");
                }

                // Insert the refund form
                insertPs.setInt(1, mscrRefundId);
                insertPs.setInt(2, formNumber);
                insertPs.setString(3, loanDate != null ? loanDate.toString() : null);
                insertPs.setBigDecimal(4, principal);
                insertPs.setBigDecimal(5, interest);
                insertPs.setBigDecimal(6, serviceCharge);
                insertPs.setBigDecimal(7, total);
                insertPs.setBigDecimal(8, BigDecimal.valueOf(noOfMonths));
                insertPs.setBigDecimal(9, BigDecimal.valueOf(collectedInterest));
                insertPs.setBigDecimal(10, totalInterest);
                insertPs.setBigDecimal(11, refund60);
                insertPs.setBigDecimal(12, refund40);
                insertPs.setString(13, remarks.toString());
                insertPs.setBoolean(14, hasBalance);
                insertPs.addBatch();
                count++;
            }

            // Execute batch
            if (count > 0) {
                insertPs.executeBatch();
            }

            loanRs.close();
            loanPs.close();
            insertPs.close();

            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get refund form data for a member's refund
     *
     * @param mscrRefundId The member_service_charge_refunds ID
     * @return List of refund form data
     */
    public List<Map<String, Object>> getRefundForms(int mscrRefundId) {
        List<Map<String, Object>> forms = new ArrayList<>();
        
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT id, form_number, date_loan, principal, interest, service_charge,
                       total, no_of_months, collected_interest, total_interest, refund_60, refund_40,
                       remarks, has_balance
                FROM member_service_charge_refund_forms
                WHERE mscr_refund_id = ? AND deleted_at IS NULL
                ORDER BY id ASC
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, mscrRefundId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> form = new java.util.HashMap<>();
                form.put("id", rs.getInt("id"));
                form.put("form_number", rs.getInt("form_number"));
                String dlStr = rs.getString("date_loan");
                if (dlStr != null && !dlStr.isEmpty()) {
                    if (dlStr.length() > 10) dlStr = dlStr.substring(0, 10);
                    form.put("date_loan", java.sql.Date.valueOf(dlStr));
                } else {
                    form.put("date_loan", null);
                }
                
                form.put("principal", rs.getBigDecimal("principal"));
                form.put("interest", rs.getBigDecimal("interest"));
                form.put("service_charge", rs.getBigDecimal("service_charge"));
                form.put("total", rs.getBigDecimal("total"));
                form.put("no_of_months", rs.getBigDecimal("no_of_months"));
                form.put("collected_interest", rs.getBigDecimal("collected_interest"));
                form.put("total_interest", rs.getBigDecimal("total_interest"));
                form.put("refund_60", rs.getBigDecimal("refund_60"));
                form.put("refund_40", rs.getBigDecimal("refund_40"));
                form.put("remarks", rs.getString("remarks"));
                form.put("has_balance", rs.getBoolean("has_balance"));
                forms.add(form);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return forms;
    }

    /**
     * Get summary aggregates for a refund
     * Sums total_interest, refund_60, refund_40 for all forms under that refund
     *
     * @param mscrRefundId The member_service_charge_refunds ID
     * @return Map with summary data
     */
    public Map<String, Object> getRefundSummary(int mscrRefundId) {
        Map<String, Object> summary = new java.util.HashMap<>();
        
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT 
                    COALESCE(SUM(total_interest), 0) as total_interest,
                    COALESCE(SUM(refund_60), 0) as refund_60,
                    COALESCE(SUM(refund_40), 0) as refund_40,
                    COUNT(*) as count
                FROM member_service_charge_refund_forms
                WHERE mscr_refund_id = ? AND deleted_at IS NULL
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, mscrRefundId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                summary.put("total_interest", rs.getBigDecimal("total_interest"));
                summary.put("refund_60", rs.getBigDecimal("refund_60"));
                summary.put("refund_40", rs.getBigDecimal("refund_40"));
                summary.put("count", rs.getInt("count"));
            }

            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summary;
    }

    /**
     * Get the most recent refund for a member
     *
     * @param memberId Member ID
     * @return Map with refund data or null if not found
     */
    public Map<String, Object> getLatestRefundForMember(int memberId) {
        Map<String, Object> refund = null;
        
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT id, description, date_from, date_to
                FROM member_service_charge_refunds
                WHERE member_id = ? AND deleted_at IS NULL
                ORDER BY created_at DESC
                LIMIT 1
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                refund = new java.util.HashMap<>();
                refund.put("id", rs.getInt("id"));
                refund.put("description", rs.getString("description"));
                
                String dfStr = rs.getString("date_from");
                if (dfStr != null && dfStr.length() > 10) dfStr = dfStr.substring(0, 10);
                refund.put("date_from", dfStr);
                
                String dtStr = rs.getString("date_to");
                if (dtStr != null && dtStr.length() > 10) dtStr = dtStr.substring(0, 10);
                refund.put("date_to", dtStr);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return refund;
    }

    /**
     * Update collected interest for a refund form and recalculate all related fields
     *
     * @param formId The refund form ID
     * @param newCollectedInterest The new collected interest value
     * @return true if update successful, false otherwise
     */
    public boolean updateCollectedInterest(int formId, BigDecimal newCollectedInterest) {
        try {
            Connection con = Database.getConnection();
            
            // First, get the current form data to recalculate
            String selectSql = """
                SELECT interest, no_of_months, remarks
                FROM member_service_charge_refund_forms
                WHERE id = ? AND deleted_at IS NULL
                """;
            PreparedStatement selectPs = con.prepareStatement(selectSql);
            selectPs.setInt(1, formId);
            ResultSet rs = selectPs.executeQuery();

            if (rs.next()) {
                BigDecimal interest = rs.getBigDecimal("interest");
                BigDecimal noOfMonths = rs.getBigDecimal("no_of_months");
                String existingRemarks = rs.getString("remarks");
                
                rs.close();
                selectPs.close();

                // Calculate total interest (pro-rata based on collected interest)
                BigDecimal totalInterest = BigDecimal.ZERO;
                if (noOfMonths != null && noOfMonths.compareTo(BigDecimal.ZERO) > 0 && interest != null) {
                    totalInterest = interest.divide(noOfMonths, 2, RoundingMode.HALF_UP)
                                          .multiply(newCollectedInterest);
                }

                // Calculate refund splits (60/40)
                BigDecimal refund60 = totalInterest.multiply(new BigDecimal("0.60")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal refund40 = totalInterest.multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP);

                // Calculate has_balance and remarks
                double remainingMonths = 0.0;
                if (noOfMonths != null) {
                    remainingMonths = Math.max(0.0, noOfMonths.doubleValue() - newCollectedInterest.doubleValue());
                }
                boolean hasBalance = remainingMonths > 0.0;
                
                StringBuilder remarks = new StringBuilder();
                if (existingRemarks != null && !existingRemarks.trim().isEmpty()) {
                    // Remove old balance/fully collected remarks if present
                    String cleanedRemarks = existingRemarks.replaceAll("Balance \\d+\\.\\d+", "")
                                                           .replaceAll("Fully collected", "")
                                                           .trim();
                    if (!cleanedRemarks.isEmpty()) {
                        remarks.append(cleanedRemarks).append(" ");
                    }
                }
                if (hasBalance) {
                    remarks.append("Balance ").append(String.format("%.1f", remainingMonths));
                } else {
                    remarks.append("Fully collected");
                }

                // Update the form
                String updateSql = """
                    UPDATE member_service_charge_refund_forms
                    SET collected_interest = ?,
                        total_interest = ?,
                        refund_60 = ?,
                        refund_40 = ?,
                        remarks = ?,
                        has_balance = ?,
                        updated_at = datetime('now')
                    WHERE id = ? AND deleted_at IS NULL
                    """;
                PreparedStatement updatePs = con.prepareStatement(updateSql);
                updatePs.setBigDecimal(1, newCollectedInterest);
                updatePs.setBigDecimal(2, totalInterest);
                updatePs.setBigDecimal(3, refund60);
                updatePs.setBigDecimal(4, refund40);
                updatePs.setString(5, remarks.toString());
                updatePs.setBoolean(6, hasBalance);
                updatePs.setInt(7, formId);
                
                int rowsAffected = updatePs.executeUpdate();
                updatePs.close();
                con.close();
                
                return rowsAffected > 0;
            } else {
                rs.close();
                selectPs.close();
                con.close();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
