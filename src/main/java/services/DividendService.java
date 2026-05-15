package services;

import com.kelsz.esla.Database;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class that computes dividend data for all members.
 *
 * Dividend algorithm (matching esla-v3):
 *   premium_percentage = member_premium_total / total_premium_all_members
 *   total_interest     = SUM(member's total_interest from refund forms)
 *   refund_60          = total_interest * 0.60
 *   refund_40          = total_interest * 0.40
 *   dividend           = premium_percentage * SUM(all refund_40)
 *   total_dividend     = refund_60 + dividend
 *
 * @author kelsz-dev
 */
public class DividendService {

    private static final BigDecimal REFUND_60_PERCENT = new BigDecimal("0.6");
    private static final BigDecimal REFUND_40_PERCENT = new BigDecimal("0.4");
    private static final int CURRENCY_SCALE = 2;
    private static final int PERCENTAGE_SCALE = 9;

    /**
     * Main entry point. Returns { "rows": List<Map>, "totals": Map }.
     */
    public Map<String, Object> getDividendData(String formDate, String nameFilter, String serviceChargeDescription) {
        List<Map<String, Object>> latestPerMember = getLatestFormDataPerMember(formDate, nameFilter);
        Map<Integer, BigDecimal> totalInterestPerMember = getTotalInterestPerMember(serviceChargeDescription);

        List<Map<String, Object>> enriched = enrichWithDividendCalculations(latestPerMember, totalInterestPerMember);

        // Sort by member name
        enriched.sort((a, b) -> {
            String na = (String) a.getOrDefault("member_name", "");
            String nb = (String) b.getOrDefault("member_name", "");
            return na.compareToIgnoreCase(nb);
        });

        Map<String, Object> totals = calculateTotals(enriched);

        Map<String, Object> result = new HashMap<>();
        result.put("rows", enriched);
        result.put("totals", totals);
        return result;
    }

    /**
     * Returns service charge details (description and date_to) for the filter combo box.
     */
    public List<Map<String, Object>> getServiceChargeDetails() {
        List<Map<String, Object>> details = new ArrayList<>();
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT description, MAX(date_to) as date_to
                FROM member_service_charge_refunds
                WHERE deleted_at IS NULL
                GROUP BY description
                ORDER BY description
                """;
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("description", rs.getString("description"));
                item.put("date_to", rs.getString("date_to"));
                details.add(item);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return details;
    }

    /**
     * Get the latest form_data entry per member for the given date,
     * optionally filtered by member name.
     */
    private List<Map<String, Object>> getLatestFormDataPerMember(String formDate, String nameFilter) {
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            Connection con = Database.getConnection();

            StringBuilder sql = new StringBuilder("""
                SELECT fd.id, fd.member_id, m.name AS member_name, fd.date, fd.premium_total
                FROM form_data fd
                INNER JOIN members m ON fd.member_id = m.id
                WHERE fd.deleted_at IS NULL AND m.deleted_at IS NULL
                """);

            List<Object> params = new ArrayList<>();

            if (formDate != null && !formDate.isEmpty()) {
                sql.append(" AND DATE(fd.date) <= ? ");
                params.add(formDate);
            }

            if (nameFilter != null && !nameFilter.isEmpty()) {
                sql.append(" AND m.name LIKE ? ");
                params.add("%" + nameFilter + "%");
            }

            sql.append(" ORDER BY fd.date DESC, fd.id DESC ");

            PreparedStatement ps = con.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();

            // Group by member_id, keep only the first (latest) entry per member
            Map<Integer, Map<String, Object>> latestByMember = new HashMap<>();
            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                if (!latestByMember.containsKey(memberId)) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("member_id", memberId);
                    row.put("member_name", rs.getString("member_name"));
                    row.put("date", rs.getString("date"));
                    row.put("premium_total", rs.getBigDecimal("premium_total"));
                    latestByMember.put(memberId, row);
                }
            }

            rs.close();
            ps.close();
            con.close();

            results.addAll(latestByMember.values());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Sum of total_interest from member_service_charge_refund_forms per member.
     * Optionally filtered by service charge description.
     *
     * @return Map of member_id -> sum of total_interest
     */
    private Map<Integer, BigDecimal> getTotalInterestPerMember(String serviceChargeDescription) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        try {
            Connection con = Database.getConnection();

            StringBuilder sql = new StringBuilder("""
                SELECT r.member_id, SUM(f.total_interest) AS sum_total_interest
                FROM member_service_charge_refund_forms f
                INNER JOIN member_service_charge_refunds r ON f.mscr_refund_id = r.id
                WHERE f.deleted_at IS NULL AND r.deleted_at IS NULL
                """);

            List<Object> params = new ArrayList<>();

            if (serviceChargeDescription != null && !serviceChargeDescription.isEmpty()) {
                sql.append(" AND r.description = ? ");
                params.add(serviceChargeDescription);
            }

            sql.append(" GROUP BY r.member_id ");

            PreparedStatement ps = con.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                BigDecimal sumInterest = rs.getBigDecimal("sum_total_interest");
                if (sumInterest == null) {
                    sumInterest = BigDecimal.ZERO;
                }
                result.put(memberId, sumInterest);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Two-pass enrichment matching esla-v3 logic:
     *  Pass 1: Calculate premium_percentage, total_interest, refund_60, refund_40
     *  Pass 2: Calculate dividend and total_dividend using pooled refund_40
     */
    private List<Map<String, Object>> enrichWithDividendCalculations(
            List<Map<String, Object>> latestPerMember,
            Map<Integer, BigDecimal> totalInterestPerMember) {

        // Total premium across all members
        BigDecimal totalPremiumSum = BigDecimal.ZERO;
        for (Map<String, Object> entry : latestPerMember) {
            BigDecimal pt = (BigDecimal) entry.get("premium_total");
            if (pt != null) {
                totalPremiumSum = totalPremiumSum.add(pt);
            }
        }

        // Pass 1: premium_percentage, total_interest, refund_60, refund_40
        for (Map<String, Object> entry : latestPerMember) {
            BigDecimal premiumTotal = (BigDecimal) entry.get("premium_total");
            if (premiumTotal == null) {
                premiumTotal = BigDecimal.ZERO;
            }

            BigDecimal premiumPercentage = BigDecimal.ZERO;
            if (totalPremiumSum.compareTo(BigDecimal.ZERO) > 0) {
                premiumPercentage = premiumTotal.divide(totalPremiumSum, PERCENTAGE_SCALE, RoundingMode.HALF_UP);
            }

            int memberId = (Integer) entry.get("member_id");
            BigDecimal memberTotalInterest = totalInterestPerMember.getOrDefault(memberId, BigDecimal.ZERO);
            BigDecimal memberRefund60 = memberTotalInterest.multiply(REFUND_60_PERCENT).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
            BigDecimal memberRefund40 = memberTotalInterest.multiply(REFUND_40_PERCENT).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);

            entry.put("premium_percentage", premiumPercentage);
            entry.put("total_interest", memberTotalInterest);
            entry.put("refund_60", memberRefund60);
            entry.put("refund_40", memberRefund40);
        }

        // Total refund_40 pool
        BigDecimal totalRefund40 = BigDecimal.ZERO;
        for (Map<String, Object> entry : latestPerMember) {
            BigDecimal r40 = (BigDecimal) entry.get("refund_40");
            if (r40 != null) {
                totalRefund40 = totalRefund40.add(r40);
            }
        }

        // Pass 2: dividend and total_dividend
        for (Map<String, Object> entry : latestPerMember) {
            BigDecimal premiumPercentage = (BigDecimal) entry.get("premium_percentage");
            BigDecimal dividend = premiumPercentage.multiply(totalRefund40).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
            BigDecimal refund60 = (BigDecimal) entry.get("refund_60");
            BigDecimal totalDividend = refund60.add(dividend);

            entry.put("dividend", dividend);
            entry.put("total_dividend", totalDividend);
        }

        return latestPerMember;
    }

    /**
     * Aggregate totals across all enriched rows.
     */
    private Map<String, Object> calculateTotals(List<Map<String, Object>> rows) {
        BigDecimal totalPremium = BigDecimal.ZERO;
        BigDecimal totalPremiumPct = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalRefund60 = BigDecimal.ZERO;
        BigDecimal totalRefund40 = BigDecimal.ZERO;
        BigDecimal totalDividend = BigDecimal.ZERO;
        BigDecimal totalTotalDividend = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {
            BigDecimal pt = (BigDecimal) row.getOrDefault("premium_total", BigDecimal.ZERO);
            BigDecimal pp = (BigDecimal) row.getOrDefault("premium_percentage", BigDecimal.ZERO);
            BigDecimal ti = (BigDecimal) row.getOrDefault("total_interest", BigDecimal.ZERO);
            BigDecimal r60 = (BigDecimal) row.getOrDefault("refund_60", BigDecimal.ZERO);
            BigDecimal r40 = (BigDecimal) row.getOrDefault("refund_40", BigDecimal.ZERO);
            BigDecimal dv = (BigDecimal) row.getOrDefault("dividend", BigDecimal.ZERO);
            BigDecimal td = (BigDecimal) row.getOrDefault("total_dividend", BigDecimal.ZERO);

            if (pt != null) totalPremium = totalPremium.add(pt);
            if (pp != null) totalPremiumPct = totalPremiumPct.add(pp);
            if (ti != null) totalInterest = totalInterest.add(ti);
            if (r60 != null) totalRefund60 = totalRefund60.add(r60);
            if (r40 != null) totalRefund40 = totalRefund40.add(r40);
            if (dv != null) totalDividend = totalDividend.add(dv);
            if (td != null) totalTotalDividend = totalTotalDividend.add(td);
        }

        Map<String, Object> totals = new HashMap<>();
        totals.put("total_premium", totalPremium);
        totals.put("premium_percentage", totalPremiumPct);
        totals.put("total_interest", totalInterest);
        totals.put("refund_60", totalRefund60);
        totals.put("refund_40", totalRefund40);
        totals.put("dividend", totalDividend);
        totals.put("total_dividend", totalTotalDividend);
        return totals;
    }
}
