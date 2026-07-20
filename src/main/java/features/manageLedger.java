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
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import com.kelsz.esla.util.ReportExporter;
import java.time.LocalDate;
import java.awt.Component;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
import ui.style;

/**
 *
 * @author kelsz-dev
 */
public class manageLedger extends javax.swing.JPanel implements ui.Exportable, ui.Refreshable {

    @Override
    public void refresh() {
        if (ledgerId <= 0 || ledgerType == null) {
            return;
        }
        int selectedMemberIndex = memberList.getSelectedIndex();
        loadMembersByLedger(ledgerType);
        if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
            memberList.setSelectedIndex(selectedMemberIndex);
            loadFormDataByLedger(ledgerId, memberIds.get(selectedMemberIndex));
            loadLoansByLedger(ledgerId, memberIds.get(selectedMemberIndex));
        } else {
            loadFormDataByLedger(ledgerId);
            loadLoansByLedger(ledgerId);
        }
    }

    private Runnable backButtonCallback;
    private int ledgerId;
    private String ledgerType;
    private java.util.List<Integer> memberIds = new java.util.ArrayList<>();
    private Timer searchTimer;
    private javax.swing.JButton deletePaymentButton;
    private javax.swing.JButton deleteLoanButton;

    private javax.swing.JButton exportPdfButton;
    private javax.swing.JButton exportExcelButton;

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
            return false;
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
     * Editable columns: Form Number (1), Date (2), Deduction Date (3), Principal (4), No. of Months (8), Remarks (10)
     * Computed/read-only columns: Service Charge (5), Interest (6), Total (7), Cutoff Amount (9)
     * ID column (0) is hidden from view
     */
    class LoanTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Only allow editing for Date (1), Deduction Date (2), Principal (3), No. of Months (7), Remarks (9)
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Integer.class; // ID
            } else if (columnIndex == 1) {
                return Date.class; // Date
            } else if (columnIndex == 2) {
                return Date.class; // Deduction Date
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
        style.applyModulePanel(this);
        style.applyToolbarPanel(jPanel1);
        
        // Fix title
        jLabel1.setText("FORM DATA");
        style.applyModernLabel(jLabel1, true);
        
        style.applyTableStyle(paymentTable, 14, 14);
        style.applyTableStyle(loanTable, 14, 14);

        // Style back button
        style.applyBackButton(backButton);
        backButton.addActionListener(this::backButtonActionPerformed);

        style.applyButton(paymentLoanButton);
        paymentLoanButton.addActionListener(this::paymentLoanButtonActionPerformed);

        // Set initial icon and text for paymentLoanButton
        updateButtonAndForm();

        // Add arrow icon to back button
        java.net.URL arrowUrl = getClass().getResource("/images/arrow-left.png");
        if (arrowUrl != null) {
            Icon arrowIcon = new ImageIcon(
                    new ImageIcon(arrowUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)
            );
            backButton.setIcon(arrowIcon);
        }
        backButton.setText("");
        backButton.setToolTipText("Back to ledgers");
        backButton.setPreferredSize(new java.awt.Dimension(34, 34));
        backButton.setMinimumSize(new java.awt.Dimension(34, 34));
        backButton.setMaximumSize(new java.awt.Dimension(34, 34));

        
        // Style search field
        style.applySearchField(formSearch);
        style.applyPlaceholder(formSearch, "Search members");
        style.applyStandardSizes(formSearch, null);
        
        // Style member list
        style.applyListStyle(memberList);
        style.applyScrollStyle(jScrollPane3);
        jScrollPane3.setPreferredSize(new java.awt.Dimension(214, 0));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(214, 120));

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
        
        setupExportButtons();
        // Add tab change listener to update button when switching tabs
    paymentTab.addChangeListener(e -> {
            updateButtonAndForm();
        });

        // Apply additional styles
        style.applyTableContainer(jScrollPane1);
        style.applyTableContainer(jScrollPane2);
        style.applyModernLabel(jLabel1, true);

        // Initialize delete buttons
        

        // Add mouse listener to paymentTable for double-click editing
                paymentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = paymentTable.rowAtPoint(evt.getPoint());
                int col = paymentTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0) {
                    int recordId = (Integer) paymentTable.getModel().getValueAt(row, 0);
                    if (col == 11) { // Edit
                        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(manageLedger.this);
                        ui.PaymentForm form = new ui.PaymentForm(ledgerId, memberIds.get(memberList.getSelectedIndex()), ledgerType, recordId);
                        javax.swing.JDialog dialog = new javax.swing.JDialog(parentFrame, "Edit Payment", true);
                        style.applyModernDialog(dialog, form, "Edit Payment");
                        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                        dialog.pack();
                        dialog.setLocationRelativeTo(parentFrame);
                        dialog.setVisible(true);
                        
                        int selectedMemberIndex = memberList.getSelectedIndex();
                        if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                            loadFormDataByLedger(ledgerId, memberIds.get(selectedMemberIndex));
                        } else {
                            loadFormDataByLedger(ledgerId, null);
                        }
                    } else if (col == 12) { // Delete
                        int confirm = style.showConfirmDialog(
                            manageLedger.this,
                            "Are you sure you want to delete this payment record?",
                            "Confirm Delete",
                            javax.swing.JOptionPane.YES_NO_OPTION
                        );
                        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                            try {
                                boolean deleted = new services.PaymentService().softDeletePayment(recordId);
                                if (deleted) {
                                    int selectedMemberIndex = memberList.getSelectedIndex();
                                    if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                                        loadFormDataByLedger(ledgerId, memberIds.get(selectedMemberIndex));
                                    } else {
                                        loadFormDataByLedger(ledgerId, null);
                                    }
                                    style.showMessageDialog(manageLedger.this, "Payment deleted successfully!");
                                } else {
                                    style.showMessageDialog(manageLedger.this, "Failed to delete payment.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (evt.getClickCount() == 2) {
                        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(manageLedger.this);
                        ui.PaymentForm form = new ui.PaymentForm(ledgerId, memberIds.get(memberList.getSelectedIndex()), ledgerType, recordId);
                        javax.swing.JDialog dialog = new javax.swing.JDialog(parentFrame, "Edit Payment", true);
                        style.applyModernDialog(dialog, form, "Edit Payment");
                        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                        dialog.pack();
                        dialog.setLocationRelativeTo(parentFrame);
                        dialog.setVisible(true);
                        
                        int selectedMemberIndex = memberList.getSelectedIndex();
                        if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                            loadFormDataByLedger(ledgerId, memberIds.get(selectedMemberIndex));
                        } else {
                            loadFormDataByLedger(ledgerId, null);
                        }
                    }
                }
            }
        });

        // Add mouse listener to loanTable for double-click editing
                loanTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = loanTable.rowAtPoint(evt.getPoint());
                int col = loanTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0) {
                    int recordId = (Integer) loanTable.getModel().getValueAt(row, 0);
                    if (col == 10) { // Edit
                        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(manageLedger.this);
                        ui.LoanForm form = new ui.LoanForm(ledgerId, memberIds.get(memberList.getSelectedIndex()), ledgerType, recordId);
                        javax.swing.JDialog dialog = new javax.swing.JDialog(parentFrame, "Edit Loan", true);
                        style.applyModernDialog(dialog, form, "Edit Loan");
                        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                        dialog.pack();
                        dialog.setLocationRelativeTo(parentFrame);
                        dialog.setVisible(true);
                        
                        int selectedMemberIndex = memberList.getSelectedIndex();
                        if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                            loadLoansByLedger(ledgerId, memberIds.get(selectedMemberIndex));
                        } else {
                            loadLoansByLedger(ledgerId, null);
                        }
                    } else if (col == 11) { // Delete
                        int confirm = style.showConfirmDialog(
                            manageLedger.this,
                            "Are you sure you want to delete this loan record?",
                            "Confirm Delete",
                            javax.swing.JOptionPane.YES_NO_OPTION
                        );
                        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                            try {
                                boolean deleted = new services.PaymentService().softDeleteLoan(recordId);
                                if (deleted) {
                                    int selectedMemberIndex = memberList.getSelectedIndex();
                                    if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                                        loadLoansByLedger(ledgerId, memberIds.get(selectedMemberIndex));
                                        loadFormDataByLedger(ledgerId, memberIds.get(selectedMemberIndex));
                                    } else {
                                        loadLoansByLedger(ledgerId, null);
                                        loadFormDataByLedger(ledgerId, null);
                                    }
                                    style.showMessageDialog(manageLedger.this, "Loan deleted successfully!");
                                } else {
                                    style.showMessageDialog(manageLedger.this, "Failed to delete loan.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (evt.getClickCount() == 2) {
                        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(manageLedger.this);
                        ui.LoanForm form = new ui.LoanForm(ledgerId, memberIds.get(memberList.getSelectedIndex()), ledgerType, recordId);
                        javax.swing.JDialog dialog = new javax.swing.JDialog(parentFrame, "Edit Loan", true);
                        style.applyModernDialog(dialog, form, "Edit Loan");
                        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                        dialog.pack();
                        dialog.setLocationRelativeTo(parentFrame);
                        dialog.setVisible(true);
                        
                        int selectedMemberIndex = memberList.getSelectedIndex();
                        if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                            loadLoansByLedger(ledgerId, memberIds.get(selectedMemberIndex));
                        } else {
                            loadLoansByLedger(ledgerId, null);
                        }
                    }
                }
            }
        });
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
            
            // Only update editable columns: Date (1), Deduction Date (2), Principal (3), No. of Months (7), Remarks (9)
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
            int confirm = style.showConfirmDialog(this,
                "Are you sure you want to update " + columnName + " to: " + (value != null ? value.toString() : "") + "?",
                "Confirm Update",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
            
            if (confirm != javax.swing.JOptionPane.YES_OPTION) {
                // Reload data to revert the change
                String searchText = style.getFieldText(formSearch).toLowerCase();
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
                // Date column
                dbColumn = "date";
                if (value instanceof Date) {
                    dbValue = new java.sql.Date(((Date) value).getTime());
                } else {
                    System.out.println("Invalid date value");
                    return;
                }
            } else if (column == 2) {
                // Deduction Date column
                dbColumn = "start_deduction_date";
                if (value instanceof Date) {
                    dbValue = new java.sql.Date(((Date) value).getTime());
                } else if (value == null) {
                    dbValue = null;
                } else {
                    System.out.println("Invalid deduction date value");
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
                sql = "UPDATE loans SET date = ?, start_deduction_date = ?, principal = ?, service_charge = ?, interest = ?, total = ?, cutoffs = ?, cutoffs_amount = ?, remarks = ? WHERE id = ?";
                ps = con.prepareStatement(sql);
                
                // Get other column values
                Object dateObj = model.getValueAt(row, 1);
                java.sql.Date date = (dateObj instanceof Date) ? new java.sql.Date(((Date) dateObj).getTime()) : null;
                
                Object deductionDateObj = model.getValueAt(row, 2);
                java.sql.Date deductionDate = (deductionDateObj instanceof Date) ? new java.sql.Date(((Date) deductionDateObj).getTime()) : null;
                
                Object remarksObj = model.getValueAt(row, 9);
                String remarks = remarksObj != null ? remarksObj.toString() : "";
                
                ps.setString(1, date != null ? date.toString() : null);
                ps.setString(2, deductionDate != null ? deductionDate.toString() : null);
                ps.setBigDecimal(3, principal);
                ps.setBigDecimal(4, serviceCharge);
                ps.setBigDecimal(5, interest);
                ps.setBigDecimal(6, total);
                ps.setInt(7, cutoffs);
                ps.setBigDecimal(8, cutoffsAmount);
                ps.setString(9, remarks);
                ps.setInt(10, recordId);
                ps.setBigDecimal(7, total);
                ps.setInt(8, cutoffs);
                ps.setBigDecimal(9, cutoffsAmount);
                ps.setString(10, remarks);
                ps.setInt(11, recordId);
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
                String searchText = style.getFieldText(formSearch).toLowerCase();
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
            style.showMessageDialog(this,
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
            int confirm = style.showConfirmDialog(this,
                "Are you sure you want to update " + columnName + " to: " + (value != null ? value.toString() : "") + "?",
                "Confirm Update",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
            
            if (confirm != javax.swing.JOptionPane.YES_OPTION) {
                // Reload data to revert the change
                String searchText = style.getFieldText(formSearch).toLowerCase();
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
                // Actual Payment column - will trigger recalculation of dependent fields
                dbColumn = "actual_payment";
                dbValue = parseCurrency(value != null ? value.toString() : "0");
            } else if (column == 8) {
                // Premium column - will trigger recalculation of dependent fields
                dbColumn = "premium";
                dbValue = parseCurrency(value != null ? value.toString() : "0");
            } else {
                return;
            }
            
            // Update the database
            Connection con = Database.getConnection();
            String sql;
            PreparedStatement ps;
            
            // If Actual Payment or Premium is updated, recalculate all dependent fields
            if (column == 3 || column == 8) {
                // Fetch current record data to calculate dependent fields
                String fetchSql = """
                    SELECT fd.ledger_id, fd.member_id, fd.date, fd.should_be_paid,
                           fd.actual_payment, fd.balance, fd.under_paid,
                           fd.scheduled_payment, fd.premium_total, fd.premium, fd.actual_payroll,
                           l.type as ledger_type
                    FROM form_data fd
                    INNER JOIN ledgers l ON fd.ledger_id = l.id
                    WHERE fd.id = ?
                """;
                PreparedStatement fetchPs = con.prepareStatement(fetchSql);
                fetchPs.setInt(1, recordId);
                ResultSet rs = fetchPs.executeQuery();
                
                if (!rs.next()) {
                    System.out.println("Record not found for ID " + recordId);
                    fetchPs.close();
                    con.close();
                    return;
                }
                
                int currentMemberId = rs.getInt("member_id");
                java.sql.Date currentDate = rs.getDate("date");
                String currentLedgerType = rs.getString("ledger_type");
                java.math.BigDecimal shouldBePaid = rs.getBigDecimal("should_be_paid");
                java.math.BigDecimal currentActualPayment = rs.getBigDecimal("actual_payment");
                java.math.BigDecimal currentBalance = rs.getBigDecimal("balance");
                java.math.BigDecimal currentUnderPaid = rs.getBigDecimal("under_paid");
                java.math.BigDecimal currentPremiumTotal = rs.getBigDecimal("premium_total");
                java.math.BigDecimal currentPremium = rs.getBigDecimal("premium");
                java.math.BigDecimal currentActualPayroll = rs.getBigDecimal("actual_payroll");
                
                rs.close();
                fetchPs.close();
                
                // Get previous entry for this member across all same-type ledgers (excluding current record)
                String previousSql = """
                    SELECT fd.id, fd.balance, fd.under_paid, fd.premium_total, fd.date
                    FROM form_data fd
                    INNER JOIN ledgers l ON fd.ledger_id = l.id
                    WHERE l.type = ? AND fd.member_id = ? AND fd.id != ? AND fd.date < ?
                    ORDER BY fd.date DESC
                    LIMIT 1
                """;
                PreparedStatement previousPs = con.prepareStatement(previousSql);
                previousPs.setString(1, currentLedgerType);
                previousPs.setInt(2, currentMemberId);
                previousPs.setInt(3, recordId);
                previousPs.setDate(4, currentDate);
                ResultSet previousRs = previousPs.executeQuery();
                
                java.math.BigDecimal previousBalance = java.math.BigDecimal.ZERO;
                java.math.BigDecimal previousPremiumTotal = java.math.BigDecimal.ZERO;
                
                if (previousRs.next()) {
                    previousBalance = previousRs.getBigDecimal("balance");
                    if (previousBalance == null) previousBalance = java.math.BigDecimal.ZERO;
                    previousPremiumTotal = previousRs.getBigDecimal("premium_total");
                    if (previousPremiumTotal == null) previousPremiumTotal = java.math.BigDecimal.ZERO;
                }
                
                previousRs.close();
                previousPs.close();
                
                // Get loan total for this date
                java.math.BigDecimal loanTotal = java.math.BigDecimal.ZERO;
                String loanSql = """
                    SELECT l.total
                    FROM loans l
                    INNER JOIN ledgers led ON l.ledger_id = led.id
                    WHERE led.type = ? AND l.member_id = ? AND l.date = ?
                    LIMIT 1
                """;
                PreparedStatement loanPs = con.prepareStatement(loanSql);
                loanPs.setString(1, currentLedgerType);
                loanPs.setInt(2, currentMemberId);
                loanPs.setDate(3, currentDate);
                ResultSet loanRs = loanPs.executeQuery();
                
                if (loanRs.next()) {
                    loanTotal = loanRs.getBigDecimal("total");
                    if (loanTotal == null) loanTotal = java.math.BigDecimal.ZERO;
                }
                
                loanRs.close();
                loanPs.close();
                
                // Calculate new values based on what changed
                java.math.BigDecimal newActualPayment = currentActualPayment;
                java.math.BigDecimal newPremium = currentPremium;
                java.math.BigDecimal newBalance = currentBalance;
                java.math.BigDecimal newUnderPaid = currentUnderPaid;
                java.math.BigDecimal newPremiumTotal = currentPremiumTotal;
                java.math.BigDecimal newActualPayroll = currentActualPayroll;
                
                if (column == 3) {
                    // Actual Payment changed
                    newActualPayment = (java.math.BigDecimal) dbValue;
                    
                    // Recalculate balance: previous_balance - new_actual_payment + loan_total
                    newBalance = previousBalance.subtract(newActualPayment).add(loanTotal);
                    
                    // Recalculate under_paid: should_be_paid - new_actual_payment (only if actual_payment < should_be_paid)
                    if (newActualPayment.compareTo(shouldBePaid) < 0) {
                        newUnderPaid = shouldBePaid.subtract(newActualPayment);
                    } else {
                        newUnderPaid = java.math.BigDecimal.ZERO;
                    }
                    
                    // Recalculate actual_payroll: new_actual_payment + premium
                    newActualPayroll = newActualPayment.add(currentPremium);
                } else if (column == 8) {
                    // Premium changed
                    newPremium = (java.math.BigDecimal) dbValue;
                    
                    // Recalculate premium_total: previous_premium_total + new_premium
                    newPremiumTotal = previousPremiumTotal.add(newPremium);
                    
                    // Recalculate actual_payroll: actual_payment + new_premium
                    newActualPayroll = currentActualPayment.add(newPremium);
                }
                
                // Update all fields including computed ones
                sql = "UPDATE form_data SET date = ?, actual_payment = ?, premium = ?, balance = ?, under_paid = ?, premium_total = ?, actual_payroll = ? WHERE id = ?";
                ps = con.prepareStatement(sql);
                
                ps.setDate(1, currentDate);
                ps.setBigDecimal(2, newActualPayment);
                ps.setBigDecimal(3, newPremium);
                ps.setBigDecimal(4, newBalance);
                ps.setBigDecimal(5, newUnderPaid);
                ps.setBigDecimal(6, newPremiumTotal);
                ps.setBigDecimal(7, newActualPayroll);
                ps.setInt(8, recordId);
            } else {
                // For Date column, just update the single column
                sql = "UPDATE form_data SET " + dbColumn + " = ? WHERE id = ?";
                ps = con.prepareStatement(sql);
                
                if (dbValue instanceof java.sql.Date) {
                    ps.setDate(1, (java.sql.Date) dbValue);
                } else if (dbValue instanceof java.math.BigDecimal) {
                    ps.setBigDecimal(1, (java.math.BigDecimal) dbValue);
                } else {
                    ps.setObject(1, dbValue);
                }
                ps.setInt(2, recordId);
            }
            
            int rowsAffected = ps.executeUpdate();
            ps.close();
            con.close();
            
            if (rowsAffected > 0) {
                System.out.println("Successfully updated " + columnName + " for record ID " + recordId);
                // Check if search filter is active
                String searchText = style.getFieldText(formSearch).toLowerCase();
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
            style.showMessageDialog(this,
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
                WHERE member_type = ? AND deleted_at IS NULL
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
                style.showMessageDialog(this,
                    "No members found for ledger type: " + ledgerType,
                    "Info",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            style.showMessageDialog(this,
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
        model.setRowCount(0);

        model.setColumnIdentifiers(new Object[]{
            "ID", "Date", "Should Be Paid", "Actual Payment",
            "Balance", "Under Paid", "Scheduled Payment", "Premium Total",
            "Premium", "Actual Payroll", "Remarks", "", ""
        });

        paymentTable.setModel(model);

        paymentTable.getColumnModel().getColumn(0).setMinWidth(0);
        paymentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        paymentTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        paymentTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        // Edit column
        paymentTable.getColumnModel().getColumn(11).setPreferredWidth(30);
        paymentTable.getColumnModel().getColumn(11).setMinWidth(30);
        paymentTable.getColumnModel().getColumn(11).setMaxWidth(30);
        
        // Delete column
        paymentTable.getColumnModel().getColumn(12).setPreferredWidth(30);
        paymentTable.getColumnModel().getColumn(12).setMinWidth(30);
        paymentTable.getColumnModel().getColumn(12).setMaxWidth(30);
        style.applyTableActionTooltips(paymentTable, java.util.Map.of(
                11, "Edit payment",
                12, "Delete payment"
        ));

        // Icons
        paymentTable.getColumnModel().getColumn(11).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private javax.swing.Icon editIcon;
            {
                java.net.URL iconUrl = getClass().getResource("/images/square-pen.png");
                if (iconUrl != null) {
                    editIcon = new javax.swing.ImageIcon(new javax.swing.ImageIcon(iconUrl).getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
                }
            }
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                javax.swing.JLabel label = (javax.swing.JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setIcon(editIcon);
                label.setText("");
                label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                return label;
            }
        });

        paymentTable.getColumnModel().getColumn(12).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private javax.swing.Icon deleteIcon;
            {
                java.net.URL iconUrl = getClass().getResource("/images/trash-2.png");
                if (iconUrl != null) {
                    deleteIcon = new javax.swing.ImageIcon(new javax.swing.ImageIcon(iconUrl).getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
                }
            }
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                javax.swing.JLabel label = (javax.swing.JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setIcon(deleteIcon);
                label.setText("");
                label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                return label;
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
                      AND fd.deleted_at IS NULL
                    ORDER BY fd.date ASC, fd.id ASC""";
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
                    WHERE fd.ledger_id = ? AND fd.deleted_at IS NULL
                    ORDER BY fd.date ASC, fd.id ASC""";
                ps = con.prepareStatement(sql);
                ps.setInt(1, ledgerId);
            }

            ResultSet rs = ps.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                String dateStr = rs.getString("date");
                java.util.Date dateVal = null;
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        if (dateStr.matches("\\d+")) {
                            dateVal = new java.sql.Date(Long.parseLong(dateStr));
                        } else {
                            if (dateStr.length() > 10) dateStr = dateStr.substring(0, 10);
                            dateVal = java.sql.Date.valueOf(dateStr);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse date: " + dateStr);
                    }
                }
                
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    dateVal,
                    formatCurrency(rs.getBigDecimal("should_be_paid")),
                    formatCurrency(rs.getBigDecimal("actual_payment")),
                    formatCurrency(rs.getBigDecimal("balance")),
                    formatCurrency(rs.getBigDecimal("under_paid")),
                    formatCurrency(rs.getBigDecimal("scheduled_payment")),
                    formatCurrency(rs.getBigDecimal("premium_total")),
                    formatCurrency(rs.getBigDecimal("premium")),
                    formatCurrency(rs.getBigDecimal("actual_payroll")),
                    rs.getString("remarks"), "", ""
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
            style.showMessageDialog(this,
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
        model.setRowCount(0);

        model.setColumnIdentifiers(new Object[]{
            "ID", "Date", "Deduction Date", "Principal", "Service Charge",
            "Interest", "Total", "No. of Months", "Cutoff Amount", "Remarks", "", ""
        });

        loanTable.setModel(model);

        loanTable.getColumnModel().getColumn(0).setMinWidth(0);
        loanTable.getColumnModel().getColumn(0).setMaxWidth(0);
        loanTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        loanTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        // Edit column
        loanTable.getColumnModel().getColumn(10).setPreferredWidth(30);
        loanTable.getColumnModel().getColumn(10).setMinWidth(30);
        loanTable.getColumnModel().getColumn(10).setMaxWidth(30);
        
        // Delete column
        loanTable.getColumnModel().getColumn(11).setPreferredWidth(30);
        loanTable.getColumnModel().getColumn(11).setMinWidth(30);
        loanTable.getColumnModel().getColumn(11).setMaxWidth(30);
        style.applyTableActionTooltips(loanTable, java.util.Map.of(
                10, "Edit loan",
                11, "Delete loan"
        ));

        // Icons
        loanTable.getColumnModel().getColumn(10).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private javax.swing.Icon editIcon;
            {
                java.net.URL iconUrl = getClass().getResource("/images/square-pen.png");
                if (iconUrl != null) {
                    editIcon = new javax.swing.ImageIcon(new javax.swing.ImageIcon(iconUrl).getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
                }
            }
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                javax.swing.JLabel label = (javax.swing.JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setIcon(editIcon);
                label.setText("");
                label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                return label;
            }
        });

        loanTable.getColumnModel().getColumn(11).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private javax.swing.Icon deleteIcon;
            {
                java.net.URL iconUrl = getClass().getResource("/images/trash-2.png");
                if (iconUrl != null) {
                    deleteIcon = new javax.swing.ImageIcon(new javax.swing.ImageIcon(iconUrl).getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
                }
            }
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                javax.swing.JLabel label = (javax.swing.JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setIcon(deleteIcon);
                label.setText("");
                label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                return label;
            }
        });

        try {
            Connection con = Database.getConnection();
            String sql;
            PreparedStatement ps;

            if (memberId != null) {
                sql = """
                    SELECT l.id, l.form_number, l.date, l.start_deduction_date, l.principal, l.service_charge,
                           l.interest, l.total, l.cutoffs, l.cutoffs_amount, l.remarks, m.name as member_name
                    FROM loans l
                    LEFT JOIN members m ON l.member_id = m.id
                    WHERE l.ledger_id = ? AND l.member_id = ?
                      AND l.deleted_at IS NULL
                    ORDER BY l.date ASC, l.id ASC""";
                ps = con.prepareStatement(sql);
                ps.setInt(1, ledgerId);
                ps.setInt(2, memberId);
            } else {
                sql = """
                    SELECT l.id, l.form_number, l.date, l.start_deduction_date, l.principal, l.service_charge,
                           l.interest, l.total, l.cutoffs, l.cutoffs_amount, l.remarks, m.name as member_name
                    FROM loans l
                    LEFT JOIN members m ON l.member_id = m.id
                    WHERE l.ledger_id = ? AND l.deleted_at IS NULL
                    ORDER BY l.date ASC, l.id ASC""";
                ps = con.prepareStatement(sql);
                ps.setInt(1, ledgerId);
            }

            ResultSet rs = ps.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                String dateStr = rs.getString("date");
                java.util.Date dateVal = null;
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        if (dateStr.matches("\\d+")) {
                            dateVal = new java.sql.Date(Long.parseLong(dateStr));
                        } else {
                            if (dateStr.length() > 10) dateStr = dateStr.substring(0, 10);
                            dateVal = java.sql.Date.valueOf(dateStr);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse date: " + dateStr);
                    }
                }

                String sddStr = rs.getString("start_deduction_date");
                java.util.Date sddVal = null;
                if (sddStr != null && !sddStr.isEmpty()) {
                    try {
                        if (sddStr.matches("\\d+")) {
                            sddVal = new java.sql.Date(Long.parseLong(sddStr));
                        } else {
                            if (sddStr.length() > 10) sddStr = sddStr.substring(0, 10);
                            sddVal = java.sql.Date.valueOf(sddStr);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse start_deduction_date: " + sddStr);
                    }
                }

                model.addRow(new Object[]{
                    rs.getInt("id"),
                    dateVal,
                    sddVal,
                    formatCurrency(rs.getBigDecimal("principal")),
                    formatCurrency(rs.getBigDecimal("service_charge")),
                    formatCurrency(rs.getBigDecimal("interest")),
                    formatCurrency(rs.getBigDecimal("total")),
                    rs.getInt("cutoffs"),
                    formatCurrency(rs.getBigDecimal("cutoffs_amount")),
                    rs.getString("remarks"), "", ""
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
            style.showMessageDialog(this,
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
        backButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Ubuntu", 1, 24)); // NOI18N
        jLabel1.setText("Form Data");

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
                .addComponent(formSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                .addContainerGap())
        );

        paymentLoanButton.setText("Add Payment");

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
                        .addComponent(paymentTab, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(formSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(paymentLoanButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(formSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(paymentLoanButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addComponent(paymentTab, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)))
        );

        backButton.setText("Back");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(40, 40, 40))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formSearchActionPerformed
        performSearch();
    }//GEN-LAST:event_formSearchActionPerformed

    /**
     * Perform search with debouncing
     */
    private void performSearch() {
        String searchText = style.getFieldText(formSearch).toLowerCase();
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

        // Prevent single-click editing - only allow double-click
        paymentTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        // Apply custom cell editor to date column (column 1)
        paymentTable.getColumnModel().getColumn(1).setCellEditor(new DateCellEditor());

        // Apply custom header renderer for sort icon
        // Removed custom header renderer to match service charge style

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

        // Prevent single-click editing - only allow double-click
        loanTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        // Apply custom cell editor to date columns (column 1 for Date, column 2 for Deduction Date)
        loanTable.getColumnModel().getColumn(1).setCellEditor(new DateCellEditor());
        loanTable.getColumnModel().getColumn(2).setCellEditor(new DateCellEditor());

        // Align No. of Months column (column 7) to the left
        javax.swing.table.DefaultTableCellRenderer leftRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                javax.swing.JLabel label = (javax.swing.JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                style.applyTableCellPadding(label);
                return label;
            }
        };
        loanTable.getColumnModel().getColumn(7).setCellRenderer(leftRenderer);

        // Apply custom header renderer for sort icon
        // Removed custom header renderer to match service charge style

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

    private void paymentLoanButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedIndex = paymentTab.getSelectedIndex();
        int selectedMemberIndex = memberList.getSelectedIndex();
        
        if (selectedMemberIndex < 0) {
            style.showMessageDialog(this, "Please select a member first", "Info", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int memberId = memberIds.get(selectedMemberIndex);
        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);

        if (selectedIndex == 0) {
            // Payment tab
            ui.PaymentForm paymentForm = new ui.PaymentForm(ledgerId, memberId, ledgerType);
            javax.swing.JDialog dialog = new javax.swing.JDialog(parentFrame, "Add Payment", true);
            style.applyModernDialog(dialog, paymentForm, "Add Payment");
            dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            
            // Refresh data
            loadFormDataByLedger(ledgerId, memberId);
        } else {
            // Loan tab
            ui.LoanForm loanForm = new ui.LoanForm(ledgerId, memberId, ledgerType);
            javax.swing.JDialog dialog = new javax.swing.JDialog(parentFrame, "Add Loan", true);
            style.applyModernDialog(dialog, loanForm, "Add Loan");
            dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            
            // Refresh data
            loadLoansByLedger(ledgerId, memberId);
        }
    }

    private void updateButtonAndForm() {
        int selectedIndex = paymentTab.getSelectedIndex();

        // Set plus icon for both Payment and Loan
        java.net.URL plusUrl = getClass().getResource("/images/plus.png");
        if (plusUrl != null) {
            Icon plusIcon = new ImageIcon(
                    new ImageIcon(plusUrl).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
                );
            paymentLoanButton.setIcon(plusIcon);
        }

        if (selectedIndex == 0) {
            paymentLoanButton.setText("Payment");
            if (deletePaymentButton != null) {
                deletePaymentButton.setVisible(true);
            }
            if (deleteLoanButton != null) {
                deleteLoanButton.setVisible(false);
            }
        } else {
            paymentLoanButton.setText("Loan");
            if (deletePaymentButton != null) {
                deletePaymentButton.setVisible(false);
            }
            if (deleteLoanButton != null) {
                deleteLoanButton.setVisible(true);
            }
        }
    }

    private void deletePaymentButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = paymentTable.getSelectedRow();
        if (selectedRow < 0) {
            style.showMessageDialog(this,
                "Please select a payment record to delete",
                "Info",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Get the record ID from column 0
        PaymentTableModel model = (PaymentTableModel) paymentTable.getModel();
        Object idObj = model.getValueAt(selectedRow, 0);
        if (idObj == null || !(idObj instanceof Integer)) {
            style.showMessageDialog(this,
                "Invalid record selected",
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        int recordId = (Integer) idObj;

        // Show confirmation dialog
        int confirm = style.showConfirmDialog(this,
            "Are you sure you want to delete this payment record?",
            "Confirm Delete",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE);

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                boolean deleted = new services.PaymentService().softDeletePayment(recordId);
                if (deleted) {
                    style.showMessageDialog(this,
                        "Payment record deleted successfully",
                        "Success",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);

                    String searchText = style.getFieldText(formSearch).toLowerCase();
                    int selectedMemberIndex = memberList.getSelectedIndex();
                    if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                        int memberId = memberIds.get(selectedMemberIndex);
                        loadFormDataByLedger(ledgerId, memberId);
                        if (!searchText.isEmpty()) {
                            filterPaymentTable(searchText);
                        }
                    } else {
                        loadFormDataByLedger(ledgerId, null);
                        if (!searchText.isEmpty()) {
                            filterPaymentTable(searchText);
                        }
                    }
                } else {
                    style.showMessageDialog(this,
                        "Failed to delete payment record",
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                style.showMessageDialog(this,
                    "Error deleting payment record: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteLoanButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = loanTable.getSelectedRow();
        if (selectedRow < 0) {
            style.showMessageDialog(this,
                "Please select a loan record to delete",
                "Info",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Get the record ID from column 0
        LoanTableModel model = (LoanTableModel) loanTable.getModel();
        Object idObj = model.getValueAt(selectedRow, 0);
        if (idObj == null || !(idObj instanceof Integer)) {
            style.showMessageDialog(this,
                "Invalid record selected",
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        int recordId = (Integer) idObj;

        // Show confirmation dialog
        int confirm = style.showConfirmDialog(this,
            "Are you sure you want to delete this loan record?",
            "Confirm Delete",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE);

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                boolean deleted = new services.PaymentService().softDeleteLoan(recordId);
                if (deleted) {
                    style.showMessageDialog(this,
                        "Loan record deleted successfully",
                        "Success",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);

                    String searchText = style.getFieldText(formSearch).toLowerCase();
                    int selectedMemberIndex = memberList.getSelectedIndex();
                    if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                        int memberId = memberIds.get(selectedMemberIndex);
                        loadLoansByLedger(ledgerId, memberId);
                        loadFormDataByLedger(ledgerId, memberId);
                        if (!searchText.isEmpty()) {
                            filterLoanTable(searchText);
                        }
                    } else {
                        loadLoansByLedger(ledgerId, null);
                        loadFormDataByLedger(ledgerId, null);
                        if (!searchText.isEmpty()) {
                            filterLoanTable(searchText);
                        }
                    }
                } else {
                    style.showMessageDialog(this,
                        "Failed to delete loan record",
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                style.showMessageDialog(this,
                    "Error deleting loan record: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void setupExportButtons() {
        removeAll();
        setLayout(new java.awt.BorderLayout(0, 16));

        JPanel titleArea = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        titleArea.setOpaque(false);
        titleArea.add(backButton);
        titleArea.add(jLabel1);
        
        // Refactor jPanel1 to use BorderLayout for better control over dynamic components
        jPanel1.setLayout(new java.awt.BorderLayout(10, 10));
        jPanel1.removeAll();
        
        JPanel headerArea = new JPanel(new java.awt.BorderLayout());
        headerArea.setBackground(style.BACKGROUND);
        
        JPanel leftHeader = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(formSearch);
        
        JPanel rightHeader = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);
        rightHeader.add(paymentLoanButton);
        
        headerArea.add(leftHeader, java.awt.BorderLayout.WEST);
        headerArea.add(rightHeader, java.awt.BorderLayout.EAST);
        
        JPanel contentArea = new JPanel(new java.awt.BorderLayout(10, 0));
        contentArea.setOpaque(false);
        jScrollPane3.setPreferredSize(new java.awt.Dimension(214, 0));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(214, 120));
        contentArea.add(jScrollPane3, java.awt.BorderLayout.WEST);
        contentArea.add(paymentTab, java.awt.BorderLayout.CENTER);
        
        jPanel1.add(headerArea, java.awt.BorderLayout.NORTH);
        jPanel1.add(contentArea, java.awt.BorderLayout.CENTER);

        add(titleArea, java.awt.BorderLayout.NORTH);
        add(jPanel1, java.awt.BorderLayout.CENTER);
        
        revalidate();
        repaint();
        jPanel1.revalidate();
        jPanel1.repaint();
    }

    @Override
    public void exportToExcel() {
        if (paymentTab.getSelectedIndex() == 0) {
            ReportExporter.exportToExcel(paymentTable, "Payment Ledger Report", "Member: " + (memberList.getSelectedValue() != null ? memberList.getSelectedValue() : "All"), new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        } else {
            ReportExporter.exportToExcel(loanTable, "Loan Ledger Report", "Member: " + (memberList.getSelectedValue() != null ? memberList.getSelectedValue() : "All"), new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        }
    }

    @Override
    public void exportToPDF() {
        if (paymentTab.getSelectedIndex() == 0) {
            ReportExporter.exportToPDF(paymentTable, "Payment Ledger Report", "Member: " + (memberList.getSelectedValue() != null ? memberList.getSelectedValue() : "All"), new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        } else {
            ReportExporter.exportToPDF(loanTable, "Loan Ledger Report", "Member: " + (memberList.getSelectedValue() != null ? memberList.getSelectedValue() : "All"), new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
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
    // End of variables declaration//GEN-END:variables
}
