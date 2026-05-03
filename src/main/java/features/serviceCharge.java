/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package features;

import com.kelsz.esla.Database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;
import services.MemberServiceChargeRefundFormService;
import ui.style;

/**
 *
 * @author kelsz-dev
 */
public class serviceCharge extends javax.swing.JPanel {

    // Store member IDs for database operations
    private List<Integer> memberIds = new ArrayList<>();
    private List<Integer> serviceChargeIds = new ArrayList<>();
    private List<Integer> formIds = new ArrayList<>();
    private MemberServiceChargeRefundFormService refundFormService;
    /** Date range for the selected service charge; drives summary table loan-date filter. */
    private java.sql.Date summaryFilterDateFrom;
    private java.sql.Date summaryFilterDateTo;
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    /** Skips combo item handler while rebuilding items so we can refresh once explicitly. */
    private boolean suppressServiceChargeItemRefresh;

    /**
     * Creates new form serviceCharge
     */
    public serviceCharge() {
        initComponents();
        refundFormService = new MemberServiceChargeRefundFormService();
        
        // Apply styles
        style.applyButton(addServiceCharge);
        try {
            java.net.URL plusIconUrl = getClass().getResource("/images/plus.png");
            if (plusIconUrl != null) {
                addServiceCharge.setIcon(new javax.swing.ImageIcon(plusIconUrl));
                addServiceCharge.setIconTextGap(8);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Custom style for editServiceCharge (icon only, transparent)
        editServiceCharge.setText("");
        try {
            java.net.URL iconUrl = getClass().getResource("/images/square-pen.png");
            if (iconUrl != null) {
                editServiceCharge.setIcon(new javax.swing.ImageIcon(iconUrl));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        editServiceCharge.setOpaque(false);
        editServiceCharge.setContentAreaFilled(false);
        editServiceCharge.setBorderPainted(false);
        editServiceCharge.setFocusPainted(false);
        editServiceCharge.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        style.applyTextField(formSearch);
        style.applyComboBox(selectServiceCharge);
        style.applyTableStyle(paymentTable, 14, 14);
        style.applyTableStyle(summaryTable, 14, 14);
        style.applyTransparentTabbedPane(memberServiceCharge);
        
        // Set correct column model for payment table
        DefaultTableModel paymentModel = new DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Member Name", "Form No", "Date", "Loan", "Interest", "Service Charge", "Total",
                "No. of Months", "Collected Interest", "Total Interest", "Refund 60%", "Refund 40%", "Remarks"
            }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing the Collected Interest column (index 8)
                return column == 8;
            }
        };
        paymentTable.setModel(paymentModel);
        
        // Set correct column model for summary table
        DefaultTableModel summaryModel = new DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Form Number", "Date", "Loan", "Interest", "Service Charge", "Total",
                "No. of Months", "Collected Interest", "Total Interest", "Refund 60%", "Refund 40%", "Remarks"
            }
        );
        summaryTable.setModel(summaryModel);
        
        // Custom renderer for summary table to style group headers and total rows
        summaryTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Check if this is a group header row (member name in uppercase, empty columns 1-11)
                if (column == 0 && value != null && !value.toString().isEmpty()) {
                    boolean isGroupHeader = true;
                    for (int i = 1; i <= 11; i++) {
                        if (table.getValueAt(row, i) != null && !table.getValueAt(row, i).toString().isEmpty()) {
                            isGroupHeader = false;
                            break;
                        }
                    }
                    if (isGroupHeader) {
                        setFont(getFont().deriveFont(java.awt.Font.BOLD));
                        setForeground(new java.awt.Color(21, 55, 143));
                        return c;
                    }
                }
                
                // Check if this is a TOTAL row (column 0 is "TOTAL", columns 1-7 empty, columns 8-10 have values)
                if (column == 0 && "TOTAL".equals(value)) {
                    boolean isTotalRow = true;
                    for (int i = 1; i <= 7; i++) {
                        if (table.getValueAt(row, i) != null && !table.getValueAt(row, i).toString().isEmpty()) {
                            isTotalRow = false;
                            break;
                        }
                    }
                    if (isTotalRow) {
                        setFont(getFont().deriveFont(java.awt.Font.BOLD));
                        setForeground(new java.awt.Color(0, 100, 0));
                        return c;
                    }
                }
                
                // Default styling for data rows
                setFont(getFont().deriveFont(java.awt.Font.PLAIN));
                setForeground(new java.awt.Color(40, 40, 40));
                
                return c;
            }
        });
        
        // Add cell editor listener for collected interest edits
        paymentTable.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int column = e.getColumn();
                int row = e.getFirstRow();
                if (column == 8) { // Collected Interest column
                    handleCollectedInterestEdit(row);
                }
            }
        });
        
        loadMembers();
        loadServiceCharges();
        loadAllMembersServiceChargeRefunds();
        addServiceCharge.addActionListener(this::addServiceChargeActionPerformed);
        editServiceCharge.addActionListener(this::editServiceChargeActionPerformed);
        
        // Add member selection listener
        memberList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = memberList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    if (selectServiceCharge.getSelectedIndex() >= 0) {
                        onServiceChargeOrMemberContextChanged();
                    } else {
                        int memberId = memberIds.get(selectedIndex);
                        loadMemberServiceChargeData(memberId);
                    }
                }
            }
        });
        
        // Add service charge selection listener
        selectServiceCharge.addItemListener(e -> {
            if (suppressServiceChargeItemRefresh) {
                return;
            }
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                onServiceChargeOrMemberContextChanged();
            }
        });

        // Add tab change listener to hide/show jPanel5 and memberList based on selected tab
        memberServiceCharge.addChangeListener(e -> {
            int selectedIndex = memberServiceCharge.getSelectedIndex();
            // Index 0 is "Member Service charge", Index 1 is "Summary"
            // Hide jPanel5 when on member service charge tab (index 0)
            jPanel5.setVisible(selectedIndex == 1);
            // Hide memberList when on summary tab (index 1) to make table full width
            jScrollPane3.setVisible(selectedIndex == 0);
        });

        javax.swing.SwingUtilities.invokeLater(this::onServiceChargeOrMemberContextChanged);
    }

    /**
     * Combo stores one representative refund id per description (global MAX(id)); member rows use different ids.
     * Resolve the selected member's refund row for the current description.
     */
    private Integer findRefundIdForMemberDescription(int memberId, String description) {
        if (description == null) {
            return null;
        }
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT id FROM member_service_charge_refunds
                WHERE member_id = ? AND description = ? AND deleted_at IS NULL
                ORDER BY created_at DESC
                LIMIT 1
                """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, memberId);
            ps.setString(2, description);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                rs.close();
                ps.close();
                con.close();
                return id;
            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void selectServiceChargeByDescription(String description) {
        if (description == null) {
            return;
        }
        for (int i = 0; i < selectServiceCharge.getItemCount(); i++) {
            if (description.equals(selectServiceCharge.getItemAt(i))) {
                selectServiceCharge.setSelectedIndex(i);
                return;
            }
        }
        if (selectServiceCharge.getItemCount() > 0) {
            selectServiceCharge.setSelectedIndex(0);
        }
    }

    /** Refresh date labels, summary filter, and member payment grid for the current combo + member selection. */
    private void onServiceChargeOrMemberContextChanged() {
        int selectedIndex = selectServiceCharge.getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        int representativeRefundId = serviceChargeIds.get(selectedIndex);
        loadServiceChargeDates(representativeRefundId);

        int memberIndex = memberList.getSelectedIndex();
        if (memberIndex >= 0) {
            int memberId = memberIds.get(memberIndex);
            String desc = (String) selectServiceCharge.getSelectedItem();
            Integer refundId = findRefundIdForMemberDescription(memberId, desc);
            if (refundId != null) {
                loadMemberServiceChargeDataByServiceCharge(memberId, refundId);
            } else {
                clearPaymentTable();
                clearSummaryTable();
            }
        } else {
            clearPaymentTable();
            clearSummaryTable();
        }
    }

    private void setDisplayDateLabels(java.sql.Date from, java.sql.Date to) {
        summaryFilterDateFrom = from;
        summaryFilterDateTo = to;
        if (from != null) {
            displayDateFrom.setText(displayDateFormat.format(from));
        } else {
            displayDateFrom.setText("—");
        }
        if (to != null) {
            displayDateTo.setText(displayDateFormat.format(to));
        } else {
            displayDateTo.setText("—");
        }
    }

    // =========================
    // FILTER FORMS BY DATE RANGE
    // =========================
    private List<Map<String, Object>> filterFormsByDateRange(List<Map<String, Object>> forms, java.sql.Date dateFrom, java.sql.Date dateTo) {
        List<Map<String, Object>> filteredForms = new ArrayList<>();
        
        if (dateFrom == null || dateTo == null) {
            // If no date range is set, return all forms
            return forms;
        }
        
        java.time.LocalDate from = dateFrom.toLocalDate();
        java.time.LocalDate to = dateTo.toLocalDate();

        for (Map<String, Object> form : forms) {
            java.sql.Date loanDate = (java.sql.Date) form.get("date_loan");
            if (loanDate != null) {
                java.time.LocalDate loan = loanDate.toLocalDate();
                // Check if loan date falls within the range (inclusive)
                if (!loan.isBefore(from) && !loan.isAfter(to)) {
                    filteredForms.add(form);
                }
            }
        }
        
        return filteredForms;
    }

    // =========================
    // LOAD SERVICE CHARGE DATES
    // =========================
    private void loadServiceChargeDates(int serviceChargeId) {
        try {
            Connection con = Database.getConnection();
            String sql = "SELECT date_from, date_to FROM member_service_charge_refunds WHERE id = ? AND deleted_at IS NULL";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, serviceChargeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                java.sql.Date dateFrom = rs.getDate("date_from");
                java.sql.Date dateTo = rs.getDate("date_to");
                setDisplayDateLabels(dateFrom, dateTo);
            } else {
                setDisplayDateLabels(null, null);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // LOAD MEMBERS FROM DATABASE
    // =========================
    private void loadMembers() {
        DefaultListModel<String> model = new DefaultListModel<>();
        memberIds.clear();

        try {
            Connection con = Database.getConnection();
            String sql = "SELECT id, name FROM members WHERE deleted_at IS NULL ORDER BY name ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                memberIds.add(id);
                model.addElement(name);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Error loading members: " + e.getMessage());
        }

        memberList.setModel(model);
    }

    // =========================
    // LOAD SERVICE CHARGES FROM DATABASE
    // =========================
    private void loadServiceCharges() {
        loadServiceCharges(null);
    }

    /**
     * @param descriptionToSelectIfPresent if non-null, select this description after reload (e.g. after edit rename).
     */
    private void loadServiceCharges(String descriptionToSelectIfPresent) {
        suppressServiceChargeItemRefresh = true;
        try {
            serviceChargeIds.clear();
            selectServiceCharge.removeAllItems();

            try {
                Connection con = Database.getConnection();
                // Get unique service charge descriptions (show only 1 per description, ordered by creation date)
                String sql = """
                    SELECT id, description FROM member_service_charge_refunds
                    WHERE deleted_at IS NULL
                    AND id IN (
                        SELECT MAX(id) FROM member_service_charge_refunds
                        WHERE deleted_at IS NULL
                        GROUP BY description
                    )
                    ORDER BY created_at DESC
                    """;
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String description = rs.getString("description");
                    serviceChargeIds.add(id);
                    selectServiceCharge.addItem(description);
                }

                rs.close();
                ps.close();
                con.close();

            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(null, "Error loading service charges: " + e.getMessage());
            }

            if (descriptionToSelectIfPresent != null) {
                selectServiceChargeByDescription(descriptionToSelectIfPresent);
            }
        } finally {
            suppressServiceChargeItemRefresh = false;
        }
    }

    // =========================
    // LOAD MEMBER SERVICE CHARGE DATA
    // =========================
    private void loadMemberServiceChargeData(int memberId) {
        // Get all refunds for this member
        List<Map<String, Object>> refunds = getAllRefundsForMember(memberId);

        if (refunds == null || refunds.isEmpty()) {
            // Clear tables if no refund exists
            clearPaymentTable();
            clearSummaryTable();
            return;
        }

        // Get member name
        String memberName = getMemberName(memberId);

        // Load all refund forms from all refunds for this member
        List<Map<String, Object>> allForms = new ArrayList<>();
        BigDecimal totalInterestSum = BigDecimal.ZERO;
        BigDecimal refund60Sum = BigDecimal.ZERO;
        BigDecimal refund40Sum = BigDecimal.ZERO;
        int totalCount = 0;

        for (Map<String, Object> refund : refunds) {
            int refundId = (Integer) refund.get("id");
            List<Map<String, Object>> forms = refundFormService.getRefundForms(refundId);
            allForms.addAll(forms);

            // Aggregate summary data
            Map<String, Object> summary = refundFormService.getRefundSummary(refundId);
            if (summary.get("total_interest") != null) {
                totalInterestSum = totalInterestSum.add((BigDecimal) summary.get("total_interest"));
            }
            if (summary.get("refund_60") != null) {
                refund60Sum = refund60Sum.add((BigDecimal) summary.get("refund_60"));
            }
            if (summary.get("refund_40") != null) {
                refund40Sum = refund40Sum.add((BigDecimal) summary.get("refund_40"));
            }
            if (summary.get("count") != null) {
                totalCount += (Integer) summary.get("count");
            }
        }

        // Use the most recent refund for date range filtering
        Map<String, Object> latestRefund = refunds.get(0);
        java.sql.Date dateFrom = (java.sql.Date) latestRefund.get("date_from");
        java.sql.Date dateTo = (java.sql.Date) latestRefund.get("date_to");

        // Filter forms by date range
        List<Map<String, Object>> filteredForms = filterFormsByDateRange(allForms, dateFrom, dateTo);

        // Set period labels before summary load so loadAllMembersServiceChargeRefunds sees the right range
        setDisplayDateLabels(dateFrom, dateTo);

        // Populate payment table with filtered forms and summary row
        populatePaymentTable(filteredForms, memberName, totalInterestSum, refund60Sum, refund40Sum);

        // Populate summary with aggregated data
        Map<String, Object> aggregatedSummary = new java.util.HashMap<>();
        aggregatedSummary.put("total_interest", totalInterestSum);
        aggregatedSummary.put("refund_60", refund60Sum);
        aggregatedSummary.put("refund_40", refund40Sum);
        aggregatedSummary.put("count", totalCount);

        populateSummaryTable(aggregatedSummary, latestRefund);
    }

    private List<Map<String, Object>> getAllRefundsForMember(int memberId) {
        List<Map<String, Object>> refunds = new ArrayList<>();

        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT id, description, date_from, date_to
                FROM member_service_charge_refunds
                WHERE member_id = ? AND deleted_at IS NULL
                ORDER BY created_at DESC
                """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> refund = new java.util.HashMap<>();
                refund.put("id", rs.getInt("id"));
                refund.put("description", rs.getString("description"));
                refund.put("date_from", rs.getDate("date_from"));
                refund.put("date_to", rs.getDate("date_to"));
                refunds.add(refund);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return refunds;
    }

    private String getMemberName(int memberId) {
        try {
            Connection con = Database.getConnection();
            String sql = "SELECT name FROM members WHERE id = ? AND deleted_at IS NULL";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                rs.close();
                ps.close();
                con.close();
                return name;
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private void loadMemberServiceChargeDataByServiceCharge(int memberId, int serviceChargeId) {
        try {
            Connection con = Database.getConnection();
            // Get the refund for this member and service charge
            String sql = "SELECT id, description, date_from, date_to FROM member_service_charge_refunds WHERE id = ? AND member_id = ? AND deleted_at IS NULL";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, serviceChargeId);
            ps.setInt(2, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> refund = new java.util.HashMap<>();
                refund.put("id", rs.getInt("id"));
                refund.put("description", rs.getString("description"));
                refund.put("date_from", rs.getDate("date_from"));
                refund.put("date_to", rs.getDate("date_to"));

                int refundId = (Integer) refund.get("id");

                // Get member name
                String memberName = getMemberName(memberId);

                // Load refund forms
                List<Map<String, Object>> forms = refundFormService.getRefundForms(refundId);

                // Filter by the selected service charge period (combo), not only this row's dates,
                // so the Member tab matches the batch range; fallback if combo dates are missing.
                java.sql.Date refundDateFrom = (java.sql.Date) refund.get("date_from");
                java.sql.Date refundDateTo = (java.sql.Date) refund.get("date_to");
                java.sql.Date filterFrom = summaryFilterDateFrom != null ? summaryFilterDateFrom : refundDateFrom;
                java.sql.Date filterTo = summaryFilterDateTo != null ? summaryFilterDateTo : refundDateTo;
                List<Map<String, Object>> filteredForms = filterFormsByDateRange(forms, filterFrom, filterTo);

                // Totals for the TOTAL row must match visible (date-filtered) rows only
                BigDecimal totalInterestSum = BigDecimal.ZERO;
                BigDecimal refund60Sum = BigDecimal.ZERO;
                BigDecimal refund40Sum = BigDecimal.ZERO;
                for (Map<String, Object> f : filteredForms) {
                    if (f.get("total_interest") != null) {
                        totalInterestSum = totalInterestSum.add((BigDecimal) f.get("total_interest"));
                    }
                    if (f.get("refund_60") != null) {
                        refund60Sum = refund60Sum.add((BigDecimal) f.get("refund_60"));
                    }
                    if (f.get("refund_40") != null) {
                        refund40Sum = refund40Sum.add((BigDecimal) f.get("refund_40"));
                    }
                }

                Map<String, Object> summary = new java.util.HashMap<>();
                summary.put("total_interest", totalInterestSum);
                summary.put("refund_60", refund60Sum);
                summary.put("refund_40", refund40Sum);
                summary.put("count", filteredForms.size());

                // Populate payment table with filtered forms and summary row
                populatePaymentTable(filteredForms, memberName, totalInterestSum, refund60Sum, refund40Sum);

                // Do not call setDisplayDateLabels here — onServiceChargeOrMemberContextChanged already
                // set period from the combo so summary + member tab stay aligned.
                populateSummaryTable(summary, refund);
            } else {
                // Clear tables if no refund exists for this combination
                clearPaymentTable();
                clearSummaryTable();
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Error loading service charge data: " + e.getMessage());
        }
    }

    private void clearPaymentTable() {
        DefaultTableModel model = (DefaultTableModel) paymentTable.getModel();
        model.setRowCount(0);
    }

    private void clearSummaryTable() {
        DefaultTableModel model = (DefaultTableModel) summaryTable.getModel();
        model.setRowCount(0);
    }

    private void populatePaymentTable(List<Map<String, Object>> forms, String memberName, BigDecimal totalInterestSum, BigDecimal refund60Sum, BigDecimal refund40Sum) {
        DefaultTableModel model = (DefaultTableModel) paymentTable.getModel();
        model.setRowCount(0);
        formIds.clear();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (Map<String, Object> form : forms) {
            Integer formId = (Integer) form.get("id");
            Integer formNumber = (Integer) form.get("form_number");
            java.sql.Date dateLoan = (java.sql.Date) form.get("date_loan");
            BigDecimal principal = (BigDecimal) form.get("principal");
            BigDecimal interest = (BigDecimal) form.get("interest");
            BigDecimal serviceCharge = (BigDecimal) form.get("service_charge");
            BigDecimal noOfMonths = (BigDecimal) form.get("no_of_months");
            BigDecimal collectedInterest = (BigDecimal) form.get("collected_interest");
            BigDecimal totalInterest = (BigDecimal) form.get("total_interest");
            BigDecimal refund60 = (BigDecimal) form.get("refund_60");
            BigDecimal refund40 = (BigDecimal) form.get("refund_40");
            String remarks = (String) form.get("remarks");
            Boolean hasBalance = (Boolean) form.get("has_balance");

            // Store form ID for editing
            formIds.add(formId);

            // Calculate displayed total based on has_balance
            // If has_balance is true: principal + interest + service_charge
            // If not: principal + interest (service charge hidden)
            BigDecimal displayedTotal;
            if (hasBalance != null && hasBalance) {
                displayedTotal = BigDecimal.ZERO;
                if (principal != null) displayedTotal = displayedTotal.add(principal);
                if (interest != null) displayedTotal = displayedTotal.add(interest);
                if (serviceCharge != null) displayedTotal = displayedTotal.add(serviceCharge);
            } else {
                displayedTotal = BigDecimal.ZERO;
                if (principal != null) displayedTotal = displayedTotal.add(principal);
                if (interest != null) displayedTotal = displayedTotal.add(interest);
            }

            model.addRow(new Object[]{
                memberName,
                formNumber,
                dateLoan != null ? dateFormat.format(dateLoan) : "",
                principal != null ? principal : "",
                interest != null ? interest : "",
                serviceCharge != null ? serviceCharge : "",
                displayedTotal,
                noOfMonths != null ? noOfMonths : "",
                collectedInterest != null ? collectedInterest : "",
                totalInterest != null ? totalInterest : "",
                refund60 != null ? refund60 : "",
                refund40 != null ? refund40 : "",
                remarks != null ? remarks : ""
            });
        }


    }

    private void populateSummaryTable(Map<String, Object> summary, Map<String, Object> refund) {
        // Update the summary labels with aggregated totals
        BigDecimal totalInterest = (BigDecimal) summary.get("total_interest");
        BigDecimal refund60 = (BigDecimal) summary.get("refund_60");
        BigDecimal refund40 = (BigDecimal) summary.get("refund_40");
        
        sumOfTotalInterest.setText(totalInterest != null ? totalInterest.toString() : "0.00");
        sumOfRefund60.setText(refund60 != null ? refund60.toString() : "0.00");
        sumOfRefund40.setText(refund40 != null ? refund40.toString() : "0.00");
        
        // Load all members' service charge refunds into the summary tab
        loadAllMembersServiceChargeRefunds();
    }
    
    private void loadAllMembersServiceChargeRefunds() {
        DefaultTableModel model = (DefaultTableModel) summaryTable.getModel();
        model.setRowCount(0);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        BigDecimal totalServiceChargeSum = BigDecimal.ZERO;
        BigDecimal totalInterestSum = BigDecimal.ZERO;
        BigDecimal totalRefund60Sum = BigDecimal.ZERO;
        BigDecimal totalRefund40Sum = BigDecimal.ZERO;
        
        java.sql.Date dateFrom = summaryFilterDateFrom;
        java.sql.Date dateTo = summaryFilterDateTo;
        String selectedDescription = null;
        if (selectServiceCharge.getSelectedIndex() >= 0 && selectServiceCharge.getSelectedItem() != null) {
            selectedDescription = selectServiceCharge.getSelectedItem().toString();
        }
        
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT m.name, f.form_number, f.date_loan, f.principal, f.interest, f.service_charge,
                       f.total, f.no_of_months, f.collected_interest, f.total_interest, f.refund_60, f.refund_40, f.remarks
                FROM member_service_charge_refund_forms f
                INNER JOIN member_service_charge_refunds r ON f.mscr_refund_id = r.id
                INNER JOIN members m ON r.member_id = m.id
                WHERE f.deleted_at IS NULL AND r.deleted_at IS NULL
                """ + (selectedDescription != null ? " AND r.description = ? " : "") + """
                ORDER BY m.name ASC, f.form_number ASC
                """;
            PreparedStatement ps = con.prepareStatement(sql);
            if (selectedDescription != null) {
                ps.setString(1, selectedDescription);
            }
            ResultSet rs = ps.executeQuery();
            
            String currentMemberName = "";
            BigDecimal memberTotalInterestSum = BigDecimal.ZERO;
            BigDecimal memberRefund60Sum = BigDecimal.ZERO;
            BigDecimal memberRefund40Sum = BigDecimal.ZERO;
            
            while (rs.next()) {
                String memberName = rs.getString("name");
                Integer formNumber = rs.getInt("form_number");
                java.sql.Date dateLoan = rs.getDate("date_loan");
                BigDecimal principal = rs.getBigDecimal("principal");
                BigDecimal interest = rs.getBigDecimal("interest");
                BigDecimal serviceCharge = rs.getBigDecimal("service_charge");
                BigDecimal total = rs.getBigDecimal("total");
                BigDecimal noOfMonths = rs.getBigDecimal("no_of_months");
                BigDecimal collectedInterest = rs.getBigDecimal("collected_interest");
                BigDecimal totalInterest = rs.getBigDecimal("total_interest");
                BigDecimal refund60 = rs.getBigDecimal("refund_60");
                BigDecimal refund40 = rs.getBigDecimal("refund_40");
                String remarks = rs.getString("remarks");
                
                // Filter by date range if dates are set
                if (dateFrom != null && dateTo != null && dateLoan != null) {
                    java.time.LocalDate loan = dateLoan.toLocalDate();
                    java.time.LocalDate from = dateFrom.toLocalDate();
                    java.time.LocalDate to = dateTo.toLocalDate();
                    if (loan.isBefore(from) || loan.isAfter(to)) {
                        continue; // Skip this form if it's outside the date range
                    }
                }
                
                // Check if member name changed - add group header and summary for previous member
                if (!memberName.equals(currentMemberName) && !currentMemberName.isEmpty()) {
                    // Add summary row for previous member
                    model.addRow(new Object[]{
                        "TOTAL",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        memberTotalInterestSum.compareTo(BigDecimal.ZERO) == 0 ? "0.00" : memberTotalInterestSum,
                        memberRefund60Sum.compareTo(BigDecimal.ZERO) == 0 ? "0.00" : memberRefund60Sum,
                        memberRefund40Sum.compareTo(BigDecimal.ZERO) == 0 ? "0.00" : memberRefund40Sum,
                        ""
                    });
                    
                    // Reset member totals
                    memberTotalInterestSum = BigDecimal.ZERO;
                    memberRefund60Sum = BigDecimal.ZERO;
                    memberRefund40Sum = BigDecimal.ZERO;
                }
                
                // Add group header for new member
                if (!memberName.equals(currentMemberName)) {
                    currentMemberName = memberName;
                    model.addRow(new Object[]{
                        memberName.toUpperCase(),
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    });
                }
                
                // Calculate displayed total based on has_balance logic
                // For summary, show the actual total from the form
                BigDecimal displayedTotal = total;
                
                model.addRow(new Object[]{
                    formNumber != null ? formNumber : "",
                    dateLoan != null ? dateFormat.format(dateLoan) : "",
                    principal != null ? principal : "",
                    interest != null ? interest : "",
                    serviceCharge != null ? serviceCharge : "",
                    displayedTotal != null ? displayedTotal : "",
                    noOfMonths != null ? noOfMonths : "",
                    collectedInterest != null ? collectedInterest : "",
                    totalInterest != null ? totalInterest : "",
                    refund60 != null ? refund60 : "",
                    refund40 != null ? refund40 : "",
                    remarks != null ? remarks : ""
                });
                
                // Accumulate member sums
                if (totalInterest != null) {
                    memberTotalInterestSum = memberTotalInterestSum.add(totalInterest);
                }
                if (refund60 != null) {
                    memberRefund60Sum = memberRefund60Sum.add(refund60);
                }
                if (refund40 != null) {
                    memberRefund40Sum = memberRefund40Sum.add(refund40);
                }
                
                // Accumulate global sums
                if (serviceCharge != null) {
                    totalServiceChargeSum = totalServiceChargeSum.add(serviceCharge);
                }
                if (totalInterest != null) {
                    totalInterestSum = totalInterestSum.add(totalInterest);
                }
                if (refund60 != null) {
                    totalRefund60Sum = totalRefund60Sum.add(refund60);
                }
                if (refund40 != null) {
                    totalRefund40Sum = totalRefund40Sum.add(refund40);
                }
            }
            
            // Add summary row for last member
            if (!currentMemberName.isEmpty()) {
                model.addRow(new Object[]{
                    "TOTAL",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    memberTotalInterestSum.compareTo(BigDecimal.ZERO) == 0 ? "0.00" : memberTotalInterestSum,
                    memberRefund60Sum.compareTo(BigDecimal.ZERO) == 0 ? "0.00" : memberRefund60Sum,
                    memberRefund40Sum.compareTo(BigDecimal.ZERO) == 0 ? "0.00" : memberRefund40Sum,
                    ""
                });
            }
            
            rs.close();
            ps.close();
            con.close();
            
            // Update sum labels
            sumOfServiceCharge.setText(totalServiceChargeSum.toString());
            sumOfTotalInterest.setText(totalInterestSum.toString());
            sumOfRefund60.setText(totalRefund60Sum.toString());
            sumOfRefund40.setText(totalRefund40Sum.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCollectedInterestEdit(int row) {
        try {
            // Get the form ID for this row
            if (row < 0 || row >= formIds.size()) {
                return;
            }
            int formId = formIds.get(row);
            
            // Get the new collected interest value from the table
            DefaultTableModel model = (DefaultTableModel) paymentTable.getModel();
            Object value = model.getValueAt(row, 8);
            
            if (value == null) {
                return;
            }
            
            // Convert to BigDecimal
            BigDecimal newCollectedInterest;
            if (value instanceof BigDecimal) {
                newCollectedInterest = (BigDecimal) value;
            } else if (value instanceof Number) {
                newCollectedInterest = BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                newCollectedInterest = new BigDecimal(value.toString());
            }
            
            // Update the database
            boolean success = refundFormService.updateCollectedInterest(formId, newCollectedInterest);
            
            if (success) {
                int memberIndex = memberList.getSelectedIndex();
                if (memberIndex >= 0) {
                    if (selectServiceCharge.getSelectedIndex() >= 0) {
                        onServiceChargeOrMemberContextChanged();
                    } else {
                        loadMemberServiceChargeData(memberIds.get(memberIndex));
                    }
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(null, "Failed to update collected interest");
            }
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Error updating collected interest: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        memberList = new javax.swing.JList<>();
        memberServiceCharge = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        paymentTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        summaryTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        sumOfRefund40 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        sumOfRefund60 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        sumOfTotalInterest = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        sumOfServiceCharge = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        selectServiceCharge = new javax.swing.JComboBox<>();
        displayDateFrom = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        displayDateTo = new javax.swing.JLabel();
        formSearch = new javax.swing.JTextField();
        editServiceCharge = new javax.swing.JButton();
        addServiceCharge = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel1.setText("SERVICE CHARGE");

        memberList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(memberList);

        paymentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(paymentTable);

        memberServiceCharge.addTab("Member Service charge", jScrollPane1);

        summaryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(summaryTable);

        memberServiceCharge.addTab("Summary", jScrollPane2);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel5.setText("Total Refund 40%");

        sumOfRefund40.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        sumOfRefund40.setText("0.00");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(sumOfRefund40))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sumOfRefund40)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel4.setText("Total Refund 60%");

        sumOfRefund60.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        sumOfRefund60.setText("0.00");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(sumOfRefund60))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sumOfRefund60)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel3.setText("Total Interest");

        sumOfTotalInterest.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        sumOfTotalInterest.setText("0.00");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(sumOfTotalInterest))
                .addContainerGap(65, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sumOfTotalInterest)
                .addContainerGap())
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel6.setText("Total Service Charge");

        sumOfServiceCharge.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        sumOfServiceCharge.setText("0.00");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(sumOfServiceCharge))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sumOfServiceCharge)
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        selectServiceCharge.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        displayDateFrom.setFont(new java.awt.Font("Dialog", 1, 15)); // NOI18N
        displayDateFrom.setText("mm/dd/yy");

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jLabel2.setText("To");

        displayDateTo.setFont(new java.awt.Font("Dialog", 1, 15)); // NOI18N
        displayDateTo.setText("mm/dd/yy");

        formSearch.addActionListener(this::formSearchActionPerformed);

        editServiceCharge.setText("Edit");
        editServiceCharge.addActionListener(this::editServiceChargeActionPerformed);

        addServiceCharge.setText("Service");
        addServiceCharge.addActionListener(this::addServiceChargeActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(formSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectServiceCharge, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayDateFrom)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayDateTo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editServiceCharge)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addServiceCharge)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(displayDateTo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(displayDateFrom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(addServiceCharge, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(editServiceCharge, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(selectServiceCharge, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                                    .addComponent(formSearch))))
                        .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(0, 491, Short.MAX_VALUE)
                                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(memberServiceCharge))))
                        .addGap(40, 40, 40))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(memberServiceCharge, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addServiceChargeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServiceChargeActionPerformed
        // Get the parent frame
        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Create the ServiceChargeForm panel
        ui.ServiceChargeForm serviceChargeFormPanel = new ui.ServiceChargeForm();

        // Wrap it in a JDialog
        javax.swing.JDialog dialog = new javax.swing.JDialog(parentFrame, "Create Service Charge", true);
        dialog.setContentPane(serviceChargeFormPanel);
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);

        String descToSelect = serviceChargeFormPanel.getLastCommittedDescription();
        loadServiceCharges(descToSelect);
        onServiceChargeOrMemberContextChanged();
        loadAllMembersServiceChargeRefunds();
    }//GEN-LAST:event_addServiceChargeActionPerformed

    private void editServiceChargeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editServiceChargeActionPerformed
        int idx = selectServiceCharge.getSelectedIndex();
        if (idx < 0 || serviceChargeIds.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select a service charge to edit.", "Edit", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        int repId = serviceChargeIds.get(idx);
        loadServiceChargeDates(repId);
        String descBefore = (String) selectServiceCharge.getSelectedItem();
        java.sql.Date from = summaryFilterDateFrom;
        java.sql.Date to = summaryFilterDateTo;

        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        ui.ServiceChargeForm panel = new ui.ServiceChargeForm(descBefore, from, to);
        javax.swing.JDialog dialog = new javax.swing.JDialog(parentFrame, "Edit Service Charge", true);
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);

        String committed = panel.getLastCommittedDescription();
        String descToSelect = committed != null ? committed : descBefore;
        loadServiceCharges(descToSelect);
        onServiceChargeOrMemberContextChanged();
        loadAllMembersServiceChargeRefunds();
    }//GEN-LAST:event_editServiceChargeActionPerformed

    private void formSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formSearchActionPerformed
        //        performSearch();
    }//GEN-LAST:event_formSearchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServiceCharge;
    private javax.swing.JLabel displayDateFrom;
    private javax.swing.JLabel displayDateTo;
    private javax.swing.JButton editServiceCharge;
    private javax.swing.JTextField formSearch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<String> memberList;
    private javax.swing.JTabbedPane memberServiceCharge;
    private javax.swing.JTable paymentTable;
    private javax.swing.JComboBox<String> selectServiceCharge;
    private javax.swing.JLabel sumOfRefund40;
    private javax.swing.JLabel sumOfRefund60;
    private javax.swing.JLabel sumOfServiceCharge;
    private javax.swing.JLabel sumOfTotalInterest;
    private javax.swing.JTable summaryTable;
    // End of variables declaration//GEN-END:variables
}
