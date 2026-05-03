/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package features;

import com.kelsz.esla.Database;
import java.awt.BorderLayout;
import java.awt.Image;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import ui.style;

/**
 *
 * @author kelsz-dev
 */
public class dashboard extends javax.swing.JPanel {

    /**
     * Creates new form dashboard
     */
    public dashboard() {
    initComponents();   // 👈 KEEP THIS (NetBeans GUI)
    style.applyTableStyle(dashboardTable, 14, 14);
    style.applyScrollStyle(jScrollPane1);
    style.applyModernLabel(jLabel1, true);
    style.applyDateChooserStyle(chooseDate);
    style.applySearchField(searchField);

    // Set default date to latest cutoff date
    LocalDate latestDate = getLatestCutoffDate();
    chooseDate.setDate(java.sql.Date.valueOf(latestDate));

    setupTable(latestDate);

    // Apply standard styling and sizes
    style.applyComboBox(memberTypeField);
    style.applyStandardSizes(searchField, memberTypeField);

    // Initialize member type filter data
    populateMemberTypes();

    // Add member type filter listener
    memberTypeField.addActionListener(evt -> filterTable());

    // Add date change listener to refresh table when date changes
    chooseDate.addPropertyChangeListener("date", evt -> {
        if (chooseDate.getDate() != null) {
            LocalDate selectedDate = new java.sql.Date(chooseDate.getDate().getTime()).toLocalDate();
            setupTable(selectedDate);
        }
    });

    // Add search functionality
    searchField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            filterTable();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filterTable();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            filterTable();
        }
    });

    }

    // =========================
    // HELPER METHODS
    // =========================
    private void populateMemberTypes() {
        try {
            Connection con = Database.getConnection();
            String sql = "SELECT DISTINCT member_type FROM members WHERE deleted_at IS NULL ORDER BY member_type ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            memberTypeField.removeAllItems();
            memberTypeField.addItem("All");

            while (rs.next()) {
                memberTypeField.addItem(rs.getString("member_type"));
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LocalDate getLatestCutoffDate() {
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT MAX(date) as latest_date
                FROM loans
                WHERE deleted_at IS NULL
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            LocalDate latestDate = null;
            if (rs.next()) {
                if (rs.getDate("latest_date") != null) {
                    latestDate = rs.getDate("latest_date").toLocalDate();
                }
            }

            rs.close();
            ps.close();
            con.close();

            // If no loans, default to today
            return latestDate != null ? latestDate : LocalDate.now();
        } catch (Exception e) {
            e.printStackTrace();
            return LocalDate.now();
        }
    }

    private java.util.Map<Integer, BigDecimal> getAllMemberPremiums() {
        java.util.Map<Integer, BigDecimal> premiums = new java.util.HashMap<>();
        try {
            Connection con = Database.getConnection();
            String sql = "SELECT id, premium FROM members WHERE deleted_at IS NULL";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int memberId = rs.getInt("id");
                BigDecimal premium = rs.getBigDecimal("premium");
                if (premium == null) {
                    premium = BigDecimal.ZERO;
                }
                premiums.put(memberId, premium);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return premiums;
    }

    private java.util.Map<Integer, BigDecimal> getAllScheduledPayments(LocalDate date) {
        java.util.Map<Integer, BigDecimal> scheduledPayments = new java.util.HashMap<>();
        try {
            Connection con = Database.getConnection();
            // Get all loans across all ledger types
            String sql = """
                SELECT l.member_id, l.date, l.start_deduction_date, l.start_deduction_on_loan_date,
                       l.total, l.cutoffs, led.type
                FROM loans l
                INNER JOIN ledgers led ON l.ledger_id = led.id
                WHERE l.deleted_at IS NULL
                ORDER BY l.member_id, l.id ASC
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                LocalDate loanDate = rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null;
                LocalDate startDeductionDate = rs.getDate("start_deduction_date") != null ? rs.getDate("start_deduction_date").toLocalDate() : null;
                Boolean startDeductionOnLoanDate = rs.getBoolean("start_deduction_on_loan_date");
                BigDecimal total = rs.getBigDecimal("total");
                Integer cutoffs = rs.getInt("cutoffs");

                if (total == null || cutoffs == null || cutoffs == 0) {
                    continue;
                }

                // Calculate loan's deduction range
                int range = cutoffs * 2;
                BigDecimal amountPerCutoff = total.divide(BigDecimal.valueOf(range), 2, java.math.RoundingMode.HALF_UP);

                // Determine effective start date
                LocalDate effectiveStartDate = getEffectiveDeductionStartDate(loanDate, startDeductionDate, startDeductionOnLoanDate);

                if (effectiveStartDate == null || date.isBefore(effectiveStartDate)) {
                    continue;
                }

                // Check if date is a cutoff date (15th or last day)
                int dayOfMonth = date.getDayOfMonth();
                int lastDayOfMonth = date.lengthOfMonth();
                if (dayOfMonth != 15 && dayOfMonth != lastDayOfMonth) {
                    continue;
                }

                // Count payment periods from effective start to date
                LocalDate currentDate = effectiveStartDate;
                int paymentPeriod = 0;
                boolean loanIsActive = false;

                while (!currentDate.isAfter(date)) {
                    int currentDay = currentDate.getDayOfMonth();
                    int currentLastDay = currentDate.lengthOfMonth();

                    if (currentDay == 15 || currentDay == currentLastDay) {
                        paymentPeriod++;
                        if (paymentPeriod >= 1 && paymentPeriod <= range && currentDate.equals(date)) {
                            loanIsActive = true;
                            break;
                        }
                    }
                    currentDate = currentDate.plusDays(1);
                }

                if (loanIsActive) {
                    scheduledPayments.merge(memberId, amountPerCutoff, BigDecimal::add);
                }
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scheduledPayments;
    }

    private java.util.Map<Integer, BigDecimal> getPreviousUnderPaid(LocalDate date) {
        java.util.Map<Integer, BigDecimal> previousUnderPaidMap = new java.util.HashMap<>();
        try {
            Connection con = Database.getConnection();
            // Get previous underpaid amount for each member across all ledger types
            String sql = """
                SELECT fd.member_id, fd.under_paid
                FROM form_data fd
                WHERE fd.date < ? AND fd.deleted_at IS NULL
                ORDER BY fd.date DESC
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();

            java.util.Set<Integer> processedMembers = new java.util.HashSet<>();

            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                if (processedMembers.contains(memberId)) {
                    continue; // Only get the most recent underpaid for each member
                }
                BigDecimal underPaid = rs.getBigDecimal("under_paid");
                if (underPaid == null) {
                    underPaid = BigDecimal.ZERO;
                }
                previousUnderPaidMap.put(memberId, underPaid);
                processedMembers.add(memberId);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return previousUnderPaidMap;
    }

    private LocalDate getEffectiveDeductionStartDate(LocalDate loanDate, LocalDate startDeductionDate, Boolean startDeductionOnLoanDate) {
        if (startDeductionOnLoanDate != null && startDeductionOnLoanDate) {
            return loanDate;
        }
        return startDeductionDate;
    }

    private void filterTable() {
        String searchText = searchField.getText().toLowerCase();
        String selectedMemberType = (String) memberTypeField.getSelectedItem();
        DefaultTableModel model = (DefaultTableModel) dashboardTable.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        dashboardTable.setRowSorter(sorter);

        if (searchText.isEmpty() && (selectedMemberType == null || selectedMemberType.equals("All"))) {
            sorter.setRowFilter(null);
        } else {
            // Create filter that checks both search text and member type
            sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String name = entry.getStringValue(0).toLowerCase();
                    boolean matchesSearch = searchText.isEmpty() || name.contains(searchText);

                    // Get member type from hidden column (index 4)
                    String memberType = entry.getStringValue(4);
                    boolean matchesMemberType = selectedMemberType == null || selectedMemberType.equals("All") ||
                            memberType.equalsIgnoreCase(selectedMemberType);

                    return matchesSearch && matchesMemberType;
                }
            });
        }
    }

    // =========================
    // TABLE DATA SETUP
    // =========================
    private void setupTable(LocalDate date) {

    DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    model.addColumn("Name");
    model.addColumn("Premium");
    model.addColumn("Loan");
    model.addColumn("Total");
    model.addColumn("MemberType"); // Hidden column for filtering

    try {
        // Fetch all data in bulk queries
        java.util.Map<Integer, BigDecimal> premiums = getAllMemberPremiums();
        java.util.Map<Integer, BigDecimal> scheduledPayments = getAllScheduledPayments(date);
        java.util.Map<Integer, BigDecimal> previousUnderPaid = getPreviousUnderPaid(date);

        Connection con = Database.getConnection();

        String sql = """
            SELECT id, member_type, name
            FROM members
            WHERE deleted_at IS NULL
            ORDER BY member_type ASC, name ASC
        """;

        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        String currentType = "";

        while (rs.next()) {
            int memberId = rs.getInt("id");
            String type = rs.getString("member_type");
            String name = rs.getString("name");

            // 👉 Insert GROUP HEADER when type changes
            if (!type.equals(currentType)) {
                currentType = type;

                model.addRow(new Object[]{
                    "" + type.toUpperCase() + "",
                    "",
                    "",
                    "",
                    type
                });
            }

            // 👉 Get premium from bulk query result
            BigDecimal premium = premiums.getOrDefault(memberId, BigDecimal.ZERO);

            // 👉 Get scheduled payment from bulk query result
            BigDecimal scheduledPayment = scheduledPayments.getOrDefault(memberId, BigDecimal.ZERO);

            // 👉 Get previous underpaid from bulk query result
            BigDecimal underPaid = previousUnderPaid.getOrDefault(memberId, BigDecimal.ZERO);

            // 👉 Calculate should_be_paid = previous underpaid + scheduled payment (PaymentService logic)
            BigDecimal loan = underPaid.add(scheduledPayment);

            // 👉 Calculate total
            BigDecimal total = premium.add(loan);

            // 👉 Insert actual member row with real data
            model.addRow(new Object[]{
                name,
                premium.compareTo(BigDecimal.ZERO) == 0 ? "-" : premium,
                loan.compareTo(BigDecimal.ZERO) == 0 ? "-" : loan,
                total.compareTo(BigDecimal.ZERO) == 0 ? "-" : total,
                type
            });
        }

        rs.close();
        ps.close();
        con.close();

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error loading members: " + e.getMessage());
    }

    dashboardTable.setModel(model);

    // Hide the MemberType column (column index 4) from view
    dashboardTable.getColumnModel().removeColumn(dashboardTable.getColumnModel().getColumn(4));

    // UI tweaks 
    dashboardTable.setRowHeight(25);
    dashboardTable.getTableHeader().setReorderingAllowed(false);

    // Custom renderer for bold member type headers
    dashboardTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Check if this is a group header row (empty columns 1, 2, 3)
            if (column == 0 && table.getValueAt(row, 1) == "" && table.getValueAt(row, 2) == "" && table.getValueAt(row, 3) == "") {
                setFont(getFont().deriveFont(java.awt.Font.BOLD));
                setForeground(new java.awt.Color(21, 55, 143));
            } else {
                setFont(getFont().deriveFont(java.awt.Font.PLAIN));
                setForeground(new java.awt.Color(40, 40, 40));
            }

            return c;
        }
    });
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        dashboardTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        searchPanel = new javax.swing.JPanel();
        searchField = new javax.swing.JTextField();
        chooseDate = new com.toedter.calendar.JDateChooser();
        memberTypeField = new javax.swing.JComboBox<>();

        setBackground(new java.awt.Color(255, 255, 255));

        dashboardTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(dashboardTable);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel1.setText("DASHBOARD");

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchField, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addContainerGap(9, Short.MAX_VALUE)
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        memberTypeField.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(memberTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(chooseDate, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(40, 40, 40))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(memberTypeField, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(chooseDate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)))
                .addGap(15, 15, 15)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser chooseDate;
    private javax.swing.JTable dashboardTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> memberTypeField;
    private javax.swing.JTextField searchField;
    private javax.swing.JPanel searchPanel;
    // End of variables declaration//GEN-END:variables
}
