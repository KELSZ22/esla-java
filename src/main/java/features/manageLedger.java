/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package features;

import com.kelsz.esla.Database;
import java.awt.Color;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;
import ui.style;

/**
 *
 * @author kelsz-dev
 */
public class manageLedger extends javax.swing.JPanel {

    private Runnable backButtonCallback;
    private int ledgerId;

    /**
     * Creates new form manageLedger
     */
    public manageLedger() {
        initComponents();
        setBackground(Color.WHITE);
        style.applyTableStyle(paymentTable, 15, 18);
        style.applyTableStyle(loanTable, 15, 18);
        
        // Style search field
        formSearchPanel.removeAll();
        formSearchPanel.setLayout(new java.awt.BorderLayout());
        
        java.net.URL searchUrl = getClass().getResource("/images/search.png");
        Icon searchIcon;
        if (searchUrl != null) {
            searchIcon = new ImageIcon(
                    new ImageIcon(searchUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)
            );
        } else {
            searchIcon = null;
        }
        
        javax.swing.JPanel searchBar = style.createSearchBar(formSearch, searchIcon);
        formSearchPanel.add(searchBar, java.awt.BorderLayout.CENTER);
        formSearchPanel.revalidate();
        formSearchPanel.repaint();
        
        // Style member list
        memberList.setFont(new java.awt.Font("Ubuntu", java.awt.Font.PLAIN, 14));
        memberList.setBackground(Color.WHITE);
        memberList.setForeground(new java.awt.Color(40, 40, 40));
        memberList.setSelectionBackground(new java.awt.Color(220, 235, 255));
        memberList.setSelectionForeground(new java.awt.Color(21, 55, 143));
        memberList.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Style tabbed pane
        paymentTab.setFont(new java.awt.Font("Ubuntu", java.awt.Font.BOLD, 14));
        paymentTab.setBackground(Color.WHITE);
        paymentTab.setForeground(new java.awt.Color(40, 40, 40));
        paymentTab.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    /**
     * Set callback for back button to return to ledger table
     * @param callback Runnable to execute when back is clicked
     */
    public void setBackButtonCallback(Runnable callback) {
        this.backButtonCallback = callback;
    }

    /**
     * Load ledger data into table
     * @param ledgerId The ledger ID to load data for
     */
    public void loadLedgerData(int ledgerId) {
        this.ledgerId = ledgerId;
        loadMembersByLedger(ledgerId);
        loadFormDataByLedger(ledgerId);
        loadLoansByLedger(ledgerId);
    }

    /**
     * Load members that belong to this ledger (via form_data)
     * @param ledgerId The ledger ID
     */
    private void loadMembersByLedger(int ledgerId) {
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT DISTINCT m.id, m.name, m.email, m.phone
                FROM members m
                INNER JOIN form_data fd ON m.id = fd.member_id
                WHERE fd.ledger_id = ?
                ORDER BY m.name
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, ledgerId);
            ResultSet rs = ps.executeQuery();

            java.util.Vector<String> memberNames = new java.util.Vector<>();
            while (rs.next()) {
                String memberInfo = rs.getString("name");
                if (rs.getString("email") != null && !rs.getString("email").isEmpty()) {
                    memberInfo += " (" + rs.getString("email") + ")";
                }
                memberNames.add(memberInfo);
            }

            memberList.setListData(memberNames);

            if (memberNames.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "No members found for ledger ID: " + ledgerId,
                    "Info",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                "Error loading members: " + e.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Load form_data records for this ledger into payment table
     * @param ledgerId The ledger ID
     */
    private void loadFormDataByLedger(int ledgerId) {
        DefaultTableModel model = (DefaultTableModel) paymentTable.getModel();
        model.setRowCount(0); // Clear existing data

        model.setColumnIdentifiers(new Object[]{
            "Form Number", "Date", "Should Be Paid", "Actual Payment",
            "Balance", "Under Paid", "Scheduled Payment", "Remarks"
        });

        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT fd.form_number, fd.date, fd.should_be_paid,
                       fd.actual_payment, fd.balance, fd.under_paid,
                       fd.scheduled_payment, fd.remarks, m.name as member_name
                FROM form_data fd
                LEFT JOIN members m ON fd.member_id = m.id
                WHERE fd.ledger_id = ?
                ORDER BY fd.date DESC
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, ledgerId);
            ResultSet rs = ps.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("form_number"),
                    rs.getDate("date"),
                    rs.getBigDecimal("should_be_paid"),
                    rs.getBigDecimal("actual_payment"),
                    rs.getBigDecimal("balance"),
                    rs.getBigDecimal("under_paid"),
                    rs.getBigDecimal("scheduled_payment"),
                    rs.getString("remarks")
                });
                rowCount++;
            }

            if (rowCount == 0) {
                System.out.println("No form_data found for ledger ID: " + ledgerId);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                "Error loading form data: " + e.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Load loan records for this ledger into loan table
     * @param ledgerId The ledger ID
     */
    private void loadLoansByLedger(int ledgerId) {
        DefaultTableModel model = (DefaultTableModel) loanTable.getModel();
        model.setRowCount(0); // Clear existing data

        model.setColumnIdentifiers(new Object[]{
            "Form Number", "Date", "Principal", "Service Charge",
            "Interest", "Total", "Cutoffs", "Remarks"
        });

        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT l.form_number, l.date, l.principal, l.service_charge,
                       l.interest, l.total, l.cutoffs, l.remarks, m.name as member_name
                FROM loans l
                LEFT JOIN members m ON l.member_id = m.id
                WHERE l.ledger_id = ?
                ORDER BY l.date DESC
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, ledgerId);
            ResultSet rs = ps.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("form_number"),
                    rs.getDate("date"),
                    rs.getBigDecimal("principal"),
                    rs.getBigDecimal("service_charge"),
                    rs.getBigDecimal("interest"),
                    rs.getBigDecimal("total"),
                    rs.getInt("cutoffs"),
                    rs.getString("remarks")
                });
                rowCount++;
            }

            if (rowCount == 0) {
                System.out.println("No loans found for ledger ID: " + ledgerId);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                "Error loading loans: " + e.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Set callback for back button to return to ledger table
     * @param callback Runnable to execute when back is clicked
     */
 

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        memberList = new javax.swing.JList<>();
        paymentTab = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        paymentTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        loanTable = new javax.swing.JTable();
        formSearchPanel = new javax.swing.JPanel();
        formSearch = new javax.swing.JTextField();
        backButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel1.setText(" FORM DATA");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        memberList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        memberList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        jScrollPane3.setViewportView(memberList);
        memberList.getAccessibleContext().setAccessibleName("");

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

        paymentTab.addTab("Payment", jScrollPane1);

        loanTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(loanTable);

        paymentTab.addTab("Loan", jScrollPane2);

        formSearch.addActionListener(this::formSearchActionPerformed);

        javax.swing.GroupLayout formSearchPanelLayout = new javax.swing.GroupLayout(formSearchPanel);
        formSearchPanel.setLayout(formSearchPanelLayout);
        formSearchPanelLayout.setHorizontalGroup(
            formSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(formSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );
        formSearchPanelLayout.setVerticalGroup(
            formSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, formSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(formSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(paymentTab, javax.swing.GroupLayout.DEFAULT_SIZE, 988, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(formSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(formSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addComponent(paymentTab, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );

        backButton.setText("Back");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(40, 40, 40))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addComponent(backButton)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(31, 31, 31))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formSearchActionPerformed
        String searchText = formSearch.getText().toLowerCase().trim();
        filterPaymentTable(searchText);
        filterLoanTable(searchText);
    }//GEN-LAST:event_formSearchActionPerformed

    /**
     * Filter payment table based on search text
     * @param searchText The search text to filter by
     */
    private void filterPaymentTable(String searchText) {
        DefaultTableModel model = (DefaultTableModel) paymentTable.getModel();
        DefaultTableModel filteredModel = new DefaultTableModel();

        // Copy column identifiers
        for (int i = 0; i < model.getColumnCount(); i++) {
            filteredModel.addColumn(model.getColumnName(i));
        }

        // Filter rows
        for (int i = 0; i < model.getRowCount(); i++) {
            boolean match = false;
            for (int j = 0; j < model.getColumnCount(); j++) {
                Object value = model.getValueAt(i, j);
                if (value != null && value.toString().toLowerCase().contains(searchText)) {
                    match = true;
                    break;
                }
            }
            if (match || searchText.isEmpty()) {
                Object[] row = new Object[model.getColumnCount()];
                for (int j = 0; j < model.getColumnCount(); j++) {
                    row[j] = model.getValueAt(i, j);
                }
                filteredModel.addRow(row);
            }
        }

        paymentTable.setModel(filteredModel);
    }

    /**
     * Filter loan table based on search text
     * @param searchText The search text to filter by
     */
    private void filterLoanTable(String searchText) {
        DefaultTableModel model = (DefaultTableModel) loanTable.getModel();
        DefaultTableModel filteredModel = new DefaultTableModel();

        // Copy column identifiers
        for (int i = 0; i < model.getColumnCount(); i++) {
            filteredModel.addColumn(model.getColumnName(i));
        }

        // Filter rows
        for (int i = 0; i < model.getRowCount(); i++) {
            boolean match = false;
            for (int j = 0; j < model.getColumnCount(); j++) {
                Object value = model.getValueAt(i, j);
                if (value != null && value.toString().toLowerCase().contains(searchText)) {
                    match = true;
                    break;
                }
            }
            if (match || searchText.isEmpty()) {
                Object[] row = new Object[model.getColumnCount()];
                for (int j = 0; j < model.getColumnCount(); j++) {
                    row[j] = model.getValueAt(i, j);
                }
                filteredModel.addRow(row);
            }
        }

        loanTable.setModel(filteredModel);
    }

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        if (backButtonCallback != null) {
            backButtonCallback.run();
        }
    }//GEN-LAST:event_backButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JTextField formSearch;
    private javax.swing.JPanel formSearchPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable loanTable;
    private javax.swing.JList<String> memberList;
    private javax.swing.JTabbedPane paymentTab;
    private javax.swing.JTable paymentTable;
    // End of variables declaration//GEN-END:variables
}
