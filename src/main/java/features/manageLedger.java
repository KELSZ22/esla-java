/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package features;

import com.kelsz.esla.Database;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import java.awt.Component;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
import ui.style;

/**
 *
 * @author kelsz-dev
 */
public class manageLedger extends javax.swing.JPanel {

    private Runnable backButtonCallback;
    private int ledgerId;
    private String ledgerType;
    private java.util.List<Integer> memberIds = new java.util.ArrayList<>();
    private Timer searchTimer;

    /**
     * Custom table model for payment table with inline editing
     * Editable columns: Date (1), Actual Payment (3), Premium (8)
     * All other columns are read-only
     * ID column (0) is hidden from view
     */
    class PaymentTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Only allow editing for Date (1), Actual Payment (3), and Premium (8)
            // ID column (0) is not editable
            return column == 1 || column == 3 || column == 8;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Integer.class; // ID
            } else if (columnIndex == 1) {
                return Date.class; // Date
            }
            return String.class;
        }
    }

    /**
     * Custom cell editor for date column using JDateChooser
     */
    class DateCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JDateChooser dateChooser;

        public DateCellEditor() {
            dateChooser = new JDateChooser();
            dateChooser.setDateFormatString("yyyy-MM-dd");
        }

        @Override
        public Object getCellEditorValue() {
            return dateChooser.getDate();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            if (value instanceof Date) {
                dateChooser.setDate((Date) value);
            }
            return dateChooser;
        }
    }

    /**
     * Custom table model for loan table with inline editing
     * Editable columns: Form Number (1), Date (2), Principal (3), No. of Months (7), Remarks (9)
     * Computed/read-only columns: Service Charge (4), Interest (5), Total (6), Cutoff Amount (8)
     * ID column (0) is hidden from view
     */
    class LoanTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Only allow editing for Form Number (1), Date (2), Principal (3), No. of Months (7), Remarks (9)
            // ID column (0), Service Charge (4), Interest (5), Total (6), and Cutoff Amount (8) are not editable (computed fields)
            return column == 1 || column == 2 || column == 3 || column == 7 || column == 9;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Integer.class; // ID
            } else if (columnIndex == 1) {
                return Integer.class; // Form Number
            } else if (columnIndex == 2) {
                return Date.class; // Date
            } else if (columnIndex == 7) {
                return Integer.class; // No. of Months
            }
            return String.class;
        }
    }

    /**
     * Creates new form manageLedger
     */
    public manageLedger() {
        initComponents();
        setBackground(Color.WHITE);
        style.applyTableStyle(paymentTable, 15, 14);
        style.applyTableStyle(loanTable, 15, 14);
        
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

        // Style scroll pane for member list
        jScrollPane3.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
            javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        jScrollPane3.setBackground(Color.WHITE);
        jScrollPane3.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane3.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(8, 8));

        // Add mouse listener to memberList
        memberList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                memberListMouseClicked(evt);
            }
        });

        // Setup debounced search
        searchTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        searchTimer.setRepeats(false);

        formSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchTimer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchTimer.restart();
            }
        });
        
        // Style tabbed pane with transparent design
        style.applyTransparentTabbedPane(paymentTab);
    }

    /**
     * Format BigDecimal value as currency string
     * @param value The BigDecimal value to format
     * @return Formatted currency string or empty string if value is null
     */
    private String formatCurrency(java.math.BigDecimal value) {
        if (value == null) {
            return "";
        }
        DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
        return currencyFormat.format(value);
    }

    /**
     * Update loan record when a cell is edited
     * @param row The row index
     * @param column The column index
     */
    private void updateLoanRecord(int row, int column) {
        try {
            LoanTableModel model = (LoanTableModel) loanTable.getModel();
            
            // Only update editable columns: Form Number (1), Date (2), Principal (3), No. of Months (7), Remarks (9)
            if (column != 1 && column != 2 && column != 3 && column != 7 && column != 9) {
                return;
            }
            
            // Get the record ID from column 0
            Object idObj = model.getValueAt(row, 0);
            if (idObj == null || !(idObj instanceof Integer)) {
                System.out.println("Invalid ID for row " + row);
                return;
            }
            int recordId = (Integer) idObj;
            
            Object value = model.getValueAt(row, column);
            String columnName = model.getColumnName(column);
            
            // Show confirmation dialog before updating
            int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Are you sure you want to update " + columnName + " to: " + (value != null ? value.toString() : "") + "?",
                "Confirm Update",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
            
            if (confirm != javax.swing.JOptionPane.YES_OPTION) {
                // Reload data to revert the change
                String searchText = formSearch.getText().toLowerCase().trim();
                if (!searchText.isEmpty()) {
                    filterLoanTable(searchText);
                } else {
                    int selectedMemberIndex = memberList.getSelectedIndex();
                    if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                        int memberId = memberIds.get(selectedMemberIndex);
                        loadLoansByLedger(ledgerId, memberId);
                    } else {
                        loadLoansByLedger(ledgerId, null);
                    }
                }
                return;
            }
            
            // Determine the database column name and value
            String dbColumn;
            Object dbValue;
            
            if (column == 1) {
                // Form Number column
                dbColumn = "form_number";
                try {
                    dbValue = Integer.parseInt(value != null ? value.toString() : "0");
                } catch (NumberFormatException e) {
                    dbValue = 0;
                }
            } else if (column == 2) {
                // Date column
                dbColumn = "date";
                if (value instanceof Date) {
                    dbValue = new java.sql.Date(((Date) value).getTime());
                } else {
                    System.out.println("Invalid date value");
                    return;
                }
            } else if (column == 3) {
                // Principal column - will trigger recalculation of computed fields
                dbColumn = "principal";
                dbValue = parseCurrency(value != null ? value.toString() : "0");
            } else if (column == 7) {
                // No. of Months column - will trigger recalculation of computed fields
                dbColumn = "cutoffs";
                try {
                    dbValue = Integer.parseInt(value != null ? value.toString() : "0");
                } catch (NumberFormatException e) {
                    dbValue = 0;
                }
            } else if (column == 9) {
                // Remarks column
                dbColumn = "remarks";
                dbValue = value != null ? value.toString() : "";
            } else {
                return;
            }
            
            // Update the database
            Connection con = Database.getConnection();
            String sql;
            PreparedStatement ps;
            
            // If Principal or No. of Months is updated, recalculate all computed fields
            if (column == 3 || column == 7) {
                // Get current values from the table
                java.math.BigDecimal principal = parseCurrency(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "0");
                Integer cutoffs = 0;
                try {
                    cutoffs = Integer.parseInt(model.getValueAt(row, 7) != null ? model.getValueAt(row, 7).toString() : "0");
                } catch (NumberFormatException e) {
                    cutoffs = 0;
                }
                
                // Calculate computed fields
                // Service Charge = principal * 0.03 (3%)
                java.math.BigDecimal serviceCharge = principal.multiply(new java.math.BigDecimal("0.03")).setScale(2, java.math.RoundingMode.HALF_UP);
                
                // Interest = principal * 0.03 * cutoffs
                java.math.BigDecimal interest = principal.multiply(new java.math.BigDecimal("0.03")).multiply(java.math.BigDecimal.valueOf(cutoffs));
                
                // Total = principal + service_charge + interest
                java.math.BigDecimal total = principal.add(serviceCharge).add(interest);
                
                // Cutoff Amount = total / (cutoffs * 2)
                java.math.BigDecimal cutoffsAmount = java.math.BigDecimal.ZERO;
                if (cutoffs > 0) {
                    cutoffsAmount = total.divide(java.math.BigDecimal.valueOf(cutoffs * 2), 2, java.math.RoundingMode.HALF_UP);
                }
                
                // Update all fields including computed ones
                sql = "UPDATE loans SET form_number = ?, date = ?, principal = ?, service_charge = ?, interest = ?, total = ?, cutoffs = ?, cutoffs_amount = ?, remarks = ? WHERE id = ?";
                ps = con.prepareStatement(sql);
                
                // Get other column values
                Object formNumberObj = model.getValueAt(row, 1);
                Integer formNumber = formNumberObj instanceof Integer ? (Integer) formNumberObj : Integer.parseInt(formNumberObj != null ? formNumberObj.toString() : "0");
                
                Object dateObj = model.getValueAt(row, 2);
                java.sql.Date date = (dateObj instanceof Date) ? new java.sql.Date(((Date) dateObj).getTime()) : null;
                
                Object remarksObj = model.getValueAt(row, 9);
                String remarks = remarksObj != null ? remarksObj.toString() : "";
                
                ps.setInt(1, formNumber);
                ps.setDate(2, date);
                ps.setBigDecimal(3, principal);
                ps.setBigDecimal(4, serviceCharge);
                ps.setBigDecimal(5, interest);
                ps.setBigDecimal(6, total);
                ps.setInt(7, cutoffs);
                ps.setBigDecimal(8, cutoffsAmount);
                ps.setString(9, remarks);
                ps.setInt(10, recordId);
            } else {
                // For other columns, just update the single column
                sql = "UPDATE loans SET " + dbColumn + " = ? WHERE id = ?";
                ps = con.prepareStatement(sql);
                
                if (dbValue instanceof java.sql.Date) {
                    ps.setDate(1, (java.sql.Date) dbValue);
                } else if (dbValue instanceof java.math.BigDecimal) {
                    ps.setBigDecimal(1, (java.math.BigDecimal) dbValue);
                } else if (dbValue instanceof Integer) {
                    ps.setInt(1, (Integer) dbValue);
                } else {
                    ps.setObject(1, dbValue);
                }
                ps.setInt(2, recordId);
            }
            
            int rowsAffected = ps.executeUpdate();
            ps.close();
            con.close();
            
            if (rowsAffected > 0) {
                System.out.println("Successfully updated " + columnName + " for loan record ID " + recordId);
                // Check if search filter is active
                String searchText = formSearch.getText().toLowerCase().trim();
                if (!searchText.isEmpty()) {
                    // Reapply the filter to preserve search state
                    filterLoanTable(searchText);
                } else {
                    // Reload data to recalculate computed fields (Total, Cutoff Amount)
                    int selectedMemberIndex = memberList.getSelectedIndex();
                    if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                        int memberId = memberIds.get(selectedMemberIndex);
                        loadLoansByLedger(ledgerId, memberId);
                    } else {
                        loadLoansByLedger(ledgerId, null);
                    }
                }
            } else {
                System.out.println("No rows affected for loan record ID " + recordId);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                "Error updating loan record: " + e.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Update payment record when a cell is edited
     * @param row The row index
     * @param column The column index
     */
    private void updatePaymentRecord(int row, int column) {
        try {
            PaymentTableModel model = (PaymentTableModel) paymentTable.getModel();
            
            // Only update editable columns: Date (1), Actual Payment (3), Premium (8)
            if (column != 1 && column != 3 && column != 8) {
                return;
            }
            
            // Get the record ID from column 0
            Object idObj = model.getValueAt(row, 0);
            if (idObj == null || !(idObj instanceof Integer)) {
                System.out.println("Invalid ID for row " + row);
                return;
            }
            int recordId = (Integer) idObj;
            
            Object value = model.getValueAt(row, column);
            String columnName = model.getColumnName(column);
            
            // Show confirmation dialog before updating
            int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Are you sure you want to update " + columnName + " to: " + (value != null ? value.toString() : "") + "?",
                "Confirm Update",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
            
            if (confirm != javax.swing.JOptionPane.YES_OPTION) {
                // Reload data to revert the change
                String searchText = formSearch.getText().toLowerCase().trim();
                if (!searchText.isEmpty()) {
                    filterPaymentTable(searchText);
                } else {
                    int selectedMemberIndex = memberList.getSelectedIndex();
                    if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                        int memberId = memberIds.get(selectedMemberIndex);
                        loadFormDataByLedger(ledgerId, memberId);
                    } else {
                        loadFormDataByLedger(ledgerId, null);
                    }
                }
                return;
            }
            
            // Determine the database column name and value
            String dbColumn;
            Object dbValue;
            
            if (column == 1) {
                // Date column
                dbColumn = "date";
                if (value instanceof Date) {
                    dbValue = new java.sql.Date(((Date) value).getTime());
                } else {
                    System.out.println("Invalid date value");
                    return;
                }
            } else if (column == 3) {
                // Actual Payment column
                dbColumn = "actual_payment";
                dbValue = parseCurrency(value != null ? value.toString() : "0");
            } else if (column == 8) {
                // Premium column
                dbColumn = "premium";
                dbValue = parseCurrency(value != null ? value.toString() : "0");
            } else {
                return;
            }
            
            // Update the database
            Connection con = Database.getConnection();
            String sql = "UPDATE form_data SET " + dbColumn + " = ? WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            
            if (dbValue instanceof java.sql.Date) {
                ps.setDate(1, (java.sql.Date) dbValue);
            } else if (dbValue instanceof java.math.BigDecimal) {
                ps.setBigDecimal(1, (java.math.BigDecimal) dbValue);
            } else {
                ps.setObject(1, dbValue);
            }
            ps.setInt(2, recordId);
            
            int rowsAffected = ps.executeUpdate();
            ps.close();
            con.close();
            
            if (rowsAffected > 0) {
                System.out.println("Successfully updated " + columnName + " for record ID " + recordId);
                // Check if search filter is active
                String searchText = formSearch.getText().toLowerCase().trim();
                if (!searchText.isEmpty()) {
                    // Reapply the filter to preserve search state
                    filterPaymentTable(searchText);
                } else {
                    // Reload data to recalculate computed fields
                    int selectedMemberIndex = memberList.getSelectedIndex();
                    if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                        int memberId = memberIds.get(selectedMemberIndex);
                        loadFormDataByLedger(ledgerId, memberId);
                    } else {
                        loadFormDataByLedger(ledgerId, null);
                    }
                }
            } else {
                System.out.println("No rows affected for record ID " + recordId);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                "Error updating payment record: " + e.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Parse currency string to BigDecimal
     * @param value The string value to parse
     * @return BigDecimal value
     */
    private java.math.BigDecimal parseCurrency(String value) {
        if (value == null || value.trim().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        // Remove commas and whitespace
        String cleaned = value.replaceAll("[,\\s]", "");
        try {
            return new java.math.BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return java.math.BigDecimal.ZERO;
        }
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
     * @param ledgerType The ledger type to filter members by
     */
    public void loadLedgerData(int ledgerId, String ledgerType) {
        this.ledgerId = ledgerId;
        this.ledgerType = ledgerType;
        loadMembersByLedger(ledgerType);
        loadFormDataByLedger(ledgerId);
        loadLoansByLedger(ledgerId);
    }

    /**
     * Load members that belong to this ledger type
     * @param ledgerType The ledger type to filter members by
     */
    private void loadMembersByLedger(String ledgerType) {
        memberIds.clear();
        try {
            Connection con = Database.getConnection();
            String sql = """
                SELECT id, name, email, phone
                FROM members
                WHERE member_type = ?
                ORDER BY name
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, ledgerType);
            ResultSet rs = ps.executeQuery();

            java.util.Vector<String> memberNames = new java.util.Vector<>();
            while (rs.next()) {
                int memberId = rs.getInt("id");
                memberIds.add(memberId);

                String memberInfo = rs.getString("name");
                memberNames.add(memberInfo);
            }

            memberList.setListData(memberNames);

            if (memberNames.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "No members found for ledger type: " + ledgerType,
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
        loadFormDataByLedger(ledgerId, null);
    }

    /**
     * Load form_data records for this ledger into payment table
     * @param ledgerId The ledger ID
     * @param memberId The member ID to filter by (null for all members)
     */
    private void loadFormDataByLedger(int ledgerId, Integer memberId) {
        PaymentTableModel model = new PaymentTableModel();
        model.setRowCount(0); // Clear existing data

        model.setColumnIdentifiers(new Object[]{
            "ID", "Date", "Should Be Paid", "Actual Payment",
            "Balance", "Under Paid", "Scheduled Payment", "Premium Total",
            "Premium", "Actual Payroll", "Remarks"
        });

        paymentTable.setModel(model);

        // Hide the ID column (column 0) from view
        paymentTable.getColumnModel().getColumn(0).setMinWidth(0);
        paymentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        paymentTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Apply custom cell editor to date column (column 1)
        paymentTable.getColumnModel().getColumn(1).setCellEditor(new DateCellEditor());

        // Add table model listener for inline editing updates
        model.addTableModelListener(new javax.swing.event.TableModelListener() {
            @Override
            public void tableChanged(javax.swing.event.TableModelEvent e) {
                if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    if (row >= 0 && column >= 0) {
                        updatePaymentRecord(row, column);
                    }
                }
            }
        });

        try {
            Connection con = Database.getConnection();
            String sql;
            PreparedStatement ps;

            if (memberId != null) {
                sql = """
                    SELECT fd.id, fd.form_number, fd.date, fd.should_be_paid,
                           fd.actual_payment, fd.balance, fd.under_paid,
                           fd.scheduled_payment, fd.premium_total, fd.premium, fd.actual_payroll,
                           fd.remarks, m.name as member_name
                    FROM form_data fd
                    LEFT JOIN members m ON fd.member_id = m.id
                    WHERE fd.ledger_id = ? AND fd.member_id = ?
                    ORDER BY fd.date DESC
                """;
                ps = con.prepareStatement(sql);
                ps.setInt(1, ledgerId);
                ps.setInt(2, memberId);
            } else {
                sql = """
                    SELECT fd.id, fd.form_number, fd.date, fd.should_be_paid,
                           fd.actual_payment, fd.balance, fd.under_paid,
                           fd.scheduled_payment, fd.premium_total, fd.premium, fd.actual_payroll,
                           fd.remarks, m.name as member_name
                    FROM form_data fd
                    LEFT JOIN members m ON fd.member_id = m.id
                    WHERE fd.ledger_id = ?
                    ORDER BY fd.date DESC
                """;
                ps = con.prepareStatement(sql);
                ps.setInt(1, ledgerId);
            }

            ResultSet rs = ps.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getDate("date"),
                    formatCurrency(rs.getBigDecimal("should_be_paid")),
                    formatCurrency(rs.getBigDecimal("actual_payment")),
                    formatCurrency(rs.getBigDecimal("balance")),
                    formatCurrency(rs.getBigDecimal("under_paid")),
                    formatCurrency(rs.getBigDecimal("scheduled_payment")),
                    formatCurrency(rs.getBigDecimal("premium_total")),
                    formatCurrency(rs.getBigDecimal("premium")),
                    formatCurrency(rs.getBigDecimal("actual_payroll")),
                    rs.getString("remarks")
                });
                rowCount++;
            }

            if (rowCount == 0) {
                System.out.println("No form_data found for ledger ID: " + ledgerId + 
                    (memberId != null ? ", member ID: " + memberId : ""));
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
        loadLoansByLedger(ledgerId, null);
    }

    /**
     * Load loan records for this ledger into loan table
     * @param ledgerId The ledger ID
     * @param memberId The member ID to filter by (null for all members)
     */
    private void loadLoansByLedger(int ledgerId, Integer memberId) {
        LoanTableModel model = new LoanTableModel();
        model.setRowCount(0); // Clear existing data

        model.setColumnIdentifiers(new Object[]{
            "ID", "Form Number", "Date", "Principal", "Service Charge",
            "Interest", "Total", "No. of Months", "Cutoff Amount", "Remarks"
        });

        loanTable.setModel(model);

        // Hide the ID column (column 0) from view
        loanTable.getColumnModel().getColumn(0).setMinWidth(0);
        loanTable.getColumnModel().getColumn(0).setMaxWidth(0);
        loanTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Apply custom cell editor to date column (column 2)
        loanTable.getColumnModel().getColumn(2).setCellEditor(new DateCellEditor());

        // Add table model listener for inline editing updates
        model.addTableModelListener(new javax.swing.event.TableModelListener() {
            @Override
            public void tableChanged(javax.swing.event.TableModelEvent e) {
                if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    if (row >= 0 && column >= 0) {
                        updateLoanRecord(row, column);
                    }
                }
            }
        });

        try {
            Connection con = Database.getConnection();
            String sql;
            PreparedStatement ps;

            if (memberId != null) {
                sql = """
                    SELECT l.id, l.form_number, l.date, l.principal, l.service_charge,
                           l.interest, l.total, l.cutoffs, l.cutoffs_amount, l.remarks, m.name as member_name
                    FROM loans l
                    LEFT JOIN members m ON l.member_id = m.id
                    WHERE l.ledger_id = ? AND l.member_id = ?
                    ORDER BY l.date DESC
                """;
                ps = con.prepareStatement(sql);
                ps.setInt(1, ledgerId);
                ps.setInt(2, memberId);
            } else {
                sql = """
                    SELECT l.id, l.form_number, l.date, l.principal, l.service_charge,
                           l.interest, l.total, l.cutoffs, l.cutoffs_amount, l.remarks, m.name as member_name
                    FROM loans l
                    LEFT JOIN members m ON l.member_id = m.id
                    WHERE l.ledger_id = ?
                    ORDER BY l.date DESC
                """;
                ps = con.prepareStatement(sql);
                ps.setInt(1, ledgerId);
            }

            ResultSet rs = ps.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("form_number"),
                    rs.getDate("date"),
                    formatCurrency(rs.getBigDecimal("principal")),
                    formatCurrency(rs.getBigDecimal("service_charge")),
                    formatCurrency(rs.getBigDecimal("interest")),
                    formatCurrency(rs.getBigDecimal("total")),
                    rs.getInt("cutoffs"),
                    formatCurrency(rs.getBigDecimal("cutoffs_amount")),
                    rs.getString("remarks")
                });
                rowCount++;
            }

            if (rowCount == 0) {
                System.out.println("No loans found for ledger ID: " + ledgerId + 
                    (memberId != null ? ", member ID: " + memberId : ""));
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
        paymentLoanButton = new javax.swing.JButton();
        rightPanel = new javax.swing.JPanel();
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

        paymentLoanButton.setText("Add Payment");
        paymentLoanButton.addActionListener(this::paymentLoanButtonActionPerformed);

        rightPanel.setBackground(new java.awt.Color(240, 240, 240));
        rightPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1));
        rightPanel.setVisible(false);

        rightPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.anchor = java.awt.GridBagConstraints.WEST;

        paymentTab.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                updateButtonAndForm();
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(paymentTab, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(formSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(paymentLoanButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(formSearchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(paymentLoanButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(paymentTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
        performSearch();
    }//GEN-LAST:event_formSearchActionPerformed

    /**
     * Perform search with debouncing
     */
    private void performSearch() {
        String searchText = formSearch.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            // If search is empty, reload all data for current member selection
            int selectedIndex = memberList.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < memberIds.size()) {
                int memberId = memberIds.get(selectedIndex);
                loadFormDataByLedger(ledgerId, memberId);
                loadLoansByLedger(ledgerId, memberId);
            } else {
                loadFormDataByLedger(ledgerId, null);
                loadLoansByLedger(ledgerId, null);
            }
        } else {
            // Filter in memory for search
            filterPaymentTable(searchText);
            filterLoanTable(searchText);
        }
    }

    private void memberListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_memberListMouseClicked
        int selectedIndex = memberList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < memberIds.size()) {
            int memberId = memberIds.get(selectedIndex);
            loadFormDataByLedger(ledgerId, memberId);
            loadLoansByLedger(ledgerId, memberId);
        } else {
            // If no selection or invalid selection, show all data
            loadFormDataByLedger(ledgerId, null);
            loadLoansByLedger(ledgerId, null);
        }
    }//GEN-LAST:event_memberListMouseClicked

    /**
     * Filter payment table based on search text
     * @param searchText The search text to filter by
     */
    private void filterPaymentTable(String searchText) {
        PaymentTableModel model = (PaymentTableModel) paymentTable.getModel();
        PaymentTableModel filteredModel = new PaymentTableModel();

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

        // Hide the ID column (column 0) from view
        paymentTable.getColumnModel().getColumn(0).setMinWidth(0);
        paymentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        paymentTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Apply custom cell editor to date column (column 1)
        paymentTable.getColumnModel().getColumn(1).setCellEditor(new DateCellEditor());

        // Add table model listener for inline editing updates
        filteredModel.addTableModelListener(new javax.swing.event.TableModelListener() {
            @Override
            public void tableChanged(javax.swing.event.TableModelEvent e) {
                if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    if (row >= 0 && column >= 0) {
                        updatePaymentRecord(row, column);
                    }
                }
            }
        });
    }

    /**
     * Filter loan table based on search text
     * @param searchText The search text to filter by
     */
    private void filterLoanTable(String searchText) {
        LoanTableModel model = (LoanTableModel) loanTable.getModel();
        LoanTableModel filteredModel = new LoanTableModel();

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

        // Hide the ID column (column 0) from view
        loanTable.getColumnModel().getColumn(0).setMinWidth(0);
        loanTable.getColumnModel().getColumn(0).setMaxWidth(0);
        loanTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Apply custom cell editor to date column (column 2)
        loanTable.getColumnModel().getColumn(2).setCellEditor(new DateCellEditor());

        // Add table model listener for inline editing updates
        filteredModel.addTableModelListener(new javax.swing.event.TableModelListener() {
            @Override
            public void tableChanged(javax.swing.event.TableModelEvent e) {
                if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    if (row >= 0 && column >= 0) {
                        updateLoanRecord(row, column);
                    }
                }
            }
        });
    }

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        if (backButtonCallback != null) {
            backButtonCallback.run();
        }
    }//GEN-LAST:event_backButtonActionPerformed

    private void paymentLoanButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentLoanButtonActionPerformed
        int selectedIndex = paymentTab.getSelectedIndex();
        if (selectedIndex == 0) {
            // Check if a member is selected
            int selectedMemberIndex = memberList.getSelectedIndex();
            if (selectedMemberIndex < 0 || selectedMemberIndex >= memberIds.size()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Please select a member first",
                    "Info",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int memberId = memberIds.get(selectedMemberIndex);

            // Show PaymentForm as modal dialog with required parameters
            java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
            ui.PaymentForm paymentForm = new ui.PaymentForm(parentFrame, ledgerId, memberId, ledgerType);
            paymentForm.setVisible(true);

            // Reload data after payment form closes
            loadFormDataByLedger(ledgerId, memberId);
        } else {
            // Check if a member is selected
            int selectedMemberIndex = memberList.getSelectedIndex();
            if (selectedMemberIndex < 0 || selectedMemberIndex >= memberIds.size()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Please select a member first",
                    "Info",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int memberId = memberIds.get(selectedMemberIndex);

            // Show LoanForm as modal dialog with required parameters
            java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
            ui.LoanForm loanForm = new ui.LoanForm(parentFrame, ledgerId, memberId, ledgerType);
            loanForm.setVisible(true);

            // Reload data after loan form closes
            loadLoansByLedger(ledgerId, memberId);
        }
    }//GEN-LAST:event_paymentLoanButtonActionPerformed

    private void updateButtonAndForm() {
        int selectedIndex = paymentTab.getSelectedIndex();

        if (selectedIndex == 0) {
            paymentLoanButton.setText("Add Payment");
        } else {
            paymentLoanButton.setText("Add Loan");
        }
    }


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
    private javax.swing.JButton paymentLoanButton;
    private javax.swing.JTabbedPane paymentTab;
    private javax.swing.JTable paymentTable;
    private javax.swing.JPanel rightPanel;
    // End of variables declaration//GEN-END:variables
}
