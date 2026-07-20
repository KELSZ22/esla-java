/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui;

import java.time.LocalDate;

/**
 *
 * @author kelsz-dev
 */
public class LoanForm extends javax.swing.JPanel {

    private int ledgerId;
    private int memberId;
    private String ledgerType;
    private int loanRecordId = -1;
    private boolean isEditMode = false;

    /**
     * Creates new form LoanForm as a modal dialog
     * @param parent The parent frame
     * @param ledgerId The ledger ID
     * @param memberId The member ID
     * @param ledgerType The ledger type
     */
    public LoanForm(int ledgerId, int memberId, String ledgerType) {
        this.ledgerId = ledgerId;
        this.memberId = memberId;
        this.ledgerType = ledgerType;
        initComponents();
        applyStyling();
    }

    public LoanForm(int ledgerId, int memberId, String ledgerType, int loanRecordId) {
        this.ledgerId = ledgerId;
        this.memberId = memberId;
        this.ledgerType = ledgerType;
        this.loanRecordId = loanRecordId;
        this.isEditMode = true;
        initComponents();
        applyStyling();
        jLabel1.setText("Edit Loan");
        loadLoanData();
    }
    
    private void loadLoanData() {
        try {
            java.sql.Connection con = com.kelsz.esla.Database.getConnection();
            String sql = "SELECT * FROM loans WHERE id = ?";
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, loanRecordId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getObject("form_number") != null) {
                    formNo.setText(String.valueOf(rs.getInt("form_number")));
                }
                String dateStr = rs.getString("date");
                if (dateStr != null && !dateStr.isEmpty()) {
                    if (dateStr.length() > 10) dateStr = dateStr.substring(0, 10);
                    jDateChooser2.setDate(java.sql.Date.valueOf(dateStr));
                }
                
                String deductionDateStr = rs.getString("start_deduction_date");
                if (deductionDateStr != null && !deductionDateStr.isEmpty()) {
                    if (deductionDateStr.length() > 10) deductionDateStr = deductionDateStr.substring(0, 10);
                    jDateChooser1.setDate(java.sql.Date.valueOf(deductionDateStr));
                }
                
                if (rs.getBigDecimal("principal") != null) {
                    jTextField1.setText(formatCurrency(rs.getBigDecimal("principal")));
                }
                if (rs.getBigDecimal("service_charge") != null) {
                    serviceCharge.setText(formatCurrency(rs.getBigDecimal("service_charge")));
                }
                if (rs.getObject("cutoffs") != null) {
                    no_of_months.setText(String.valueOf(rs.getInt("cutoffs")));
                }
                remarks.setText(rs.getString("remarks"));
            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String formatCurrency(java.math.BigDecimal value) {
        if (value == null) return "0.00";
        java.text.DecimalFormat currencyFormat = new java.text.DecimalFormat("#,##0.00");
        return currencyFormat.format(value);
    }

    private void applyStyling() {
        style.applyFormPanel(this);

        // Apply text field styling
        style.applyTextField(formNo);
        style.applyTextField(serviceCharge);
        style.applyTextField(no_of_months);
        style.applyTextField(jTextField1);
        style.applyPlaceholder(formNo, "Auto if blank");
        style.applyPlaceholder(serviceCharge, "Optional service charge %");
        style.applyPlaceholder(no_of_months, "Enter number of months");
        style.applyPlaceholder(jTextField1, "Enter principal amount");

        // Apply button styling
        style.applyButton(saveLoanButton);
        style.applySecondaryButton(loanCanvelButton);

        style.applyTextArea(remarks);
        style.applyPlaceholder(remarks, "Enter remarks");

        // Style date choosers
        style.applyDateChooserStyle(jDateChooser1);
        style.applyDateChooserStyle(jDateChooser2);
        
        style.applyModernLabel(jLabel1, true);
        style.applyModernLabel(jLabel2, false);
        style.applyModernLabel(jLabel3, false);
        style.applyModernLabel(jLabel4, false);
        style.applyModernLabel(jLabel5, false);
        style.applyModernLabel(jLabel6, false);
        style.applyModernLabel(jLabel7, false);
        style.applyModernLabel(dateLabel, false);
        
        jLabel1.setVisible(false);
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
        jLabel2 = new javax.swing.JLabel();
        formNo = new javax.swing.JTextField();
        dateLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        serviceCharge = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        no_of_months = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        remarks = new javax.swing.JTextArea();
        saveLoanButton = new javax.swing.JButton();
        loanCanvelButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jTextField1 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel1.setText("Create Loan");

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel2.setText("Form no.");

        formNo.addActionListener(this::formNoActionPerformed);

        dateLabel.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        dateLabel.setText("Date");

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel3.setText("Service charge %");

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel4.setText("No. of months");

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel5.setText("Remarks");

        remarks.setColumns(20);
        remarks.setRows(5);
        jScrollPane1.setViewportView(remarks);

        saveLoanButton.setText("Save");
        saveLoanButton.addActionListener(this::saveLoanButtonActionPerformed);

        loanCanvelButton.setText("Cancel");
        loanCanvelButton.addActionListener(this::loanCanvelButtonActionPerformed);

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel6.setText("Deduction date");

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel7.setText("Principal");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(loanCanvelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveLoanButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(formNo, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(serviceCharge, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dateLabel)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7)
                            .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(no_of_months, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(dateLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(formNo))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceCharge, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(no_of_months, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField1)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveLoanButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loanCanvelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formNoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_formNoActionPerformed

    private void saveLoanButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveLoanButtonActionPerformed
        // Validate form inputs
        if (!validateForm()) {
            return;
        }

        try {
            // Parse form inputs
            java.util.Date selectedDate = jDateChooser2.getDate();
            if (selectedDate == null) {
                style.showMessageDialog(this,
                    "Please select a date",
                    "Validation Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate loanDate = selectedDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

            java.util.Date selectedDeductionDate = jDateChooser1.getDate();
            LocalDate startDeductionDate = null;
            if (selectedDeductionDate != null) {
                startDeductionDate = selectedDeductionDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            }

            Integer formNumber = style.getFieldText(formNo).isEmpty() ? null : Integer.parseInt(style.getFieldText(formNo));
            java.math.BigDecimal principal = parseDecimal(style.getFieldText(jTextField1));
            java.math.BigDecimal serviceChargeValue = parseDecimal(style.getFieldText(serviceCharge));
            Integer noOfMonths = style.getFieldText(no_of_months).isEmpty() ? null : Integer.parseInt(style.getFieldText(no_of_months));
            String remarks = style.getFieldText(this.remarks);

            if (isEditMode) {
                // Calculate computed fields
                java.math.BigDecimal p = principal != null ? principal : java.math.BigDecimal.ZERO;
                int cutoffs = noOfMonths != null ? noOfMonths : 0;
                
                java.math.BigDecimal serviceChargeCalculated = p.multiply(new java.math.BigDecimal("0.03")).setScale(2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal interest = p.multiply(new java.math.BigDecimal("0.03")).multiply(java.math.BigDecimal.valueOf(cutoffs));
                java.math.BigDecimal total = p.add(serviceChargeCalculated).add(interest);
                
                java.math.BigDecimal cutoffsAmount = java.math.BigDecimal.ZERO;
                if (cutoffs > 0) {
                    cutoffsAmount = total.divide(java.math.BigDecimal.valueOf(cutoffs * 2), 2, java.math.RoundingMode.HALF_UP);
                }
                
                java.sql.Connection con = com.kelsz.esla.Database.getConnection();
                String sql = "UPDATE loans SET form_number=?, date=?, start_deduction_date=?, principal=?, service_charge=?, interest=?, total=?, cutoffs=?, cutoffs_amount=?, service_charge_balance=?, interest_balance=?, remarks=?, updated_at=datetime('now') WHERE id=?";
                java.sql.PreparedStatement ps = con.prepareStatement(sql);
                java.math.BigDecimal rate = new java.math.BigDecimal("0.03");
                ps.setObject(1, formNumber);
                ps.setString(2, loanDate != null ? loanDate.toString() : null);
                ps.setString(3, startDeductionDate != null ? startDeductionDate.toString() : null);
                ps.setBigDecimal(4, p);
                ps.setBigDecimal(5, serviceChargeCalculated);
                ps.setBigDecimal(6, interest);
                ps.setBigDecimal(7, total);
                ps.setInt(8, cutoffs);
                ps.setBigDecimal(9, cutoffsAmount);
                ps.setBigDecimal(10, rate);
                ps.setBigDecimal(11, rate);
                ps.setString(12, remarks);
                ps.setInt(13, loanRecordId);
                
                int rowsAffected = ps.executeUpdate();
                ps.close();
                con.close();

                if (rowsAffected > 0 && ledgerType != null) {
                    new services.PaymentService().recomputeMemberChain(memberId, ledgerType, loanDate);
                }
                
                if (rowsAffected > 0) {
                    style.showMessageDialog(this,
                        "Loan updated successfully!",
                        "Success",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
                } else {
                    style.showMessageDialog(this,
                        "Failed to update loan. Please try again.",
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Create loan using LoanService
                services.LoanService loanService = new services.LoanService();
                int loanId = loanService.createLoan(
                    ledgerId, memberId, formNumber, loanDate,
                    principal, serviceChargeValue, noOfMonths, startDeductionDate, null, remarks
                );

                if (loanId > 0) {
                    style.showMessageDialog(this,
                        "Loan created successfully!",
                        "Success",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
                } else {
                    style.showMessageDialog(this,
                        "Failed to create loan. Please try again.",
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            style.showMessageDialog(this,
                "Please enter valid numeric values for form number and number of months",
                "Validation Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            style.showMessageDialog(this,
                "Error creating loan: " + e.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveLoanButtonActionPerformed

    private void loanCanvelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loanCanvelButtonActionPerformed
        javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
    }//GEN-LAST:event_loanCanvelButtonActionPerformed

    /**
     * Validate form inputs
     * @return true if valid, false otherwise
     */
    private boolean validateForm() {
        style.clearFieldError(formNo);
        style.clearFieldError(jDateChooser2);
        style.clearFieldError(jDateChooser1);
        style.clearFieldError(serviceCharge);
        style.clearFieldError(no_of_months);
        style.clearFieldError(jTextField1);

        if (jDateChooser2.getDate() == null) {
            style.showFieldError(jDateChooser2);
            style.showMessageDialog(this,
                "Please select a date",
                "Validation Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (style.getFieldText(jTextField1).isEmpty()) {
            style.showFieldError(jTextField1);
            style.showMessageDialog(this,
                "Principal is required",
                "Validation Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (style.getFieldText(no_of_months).isEmpty()) {
            style.showFieldError(no_of_months);
            style.showMessageDialog(this,
                "Number of months is required",
                "Validation Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            if (!style.getFieldText(formNo).isEmpty()) {
                Integer.parseInt(style.getFieldText(formNo));
            }
            if (!style.getFieldText(no_of_months).isEmpty()) {
                Integer.parseInt(style.getFieldText(no_of_months));
            }
            if (!style.getFieldText(jTextField1).isEmpty()) {
                parseDecimal(style.getFieldText(jTextField1));
            }
            if (!style.getFieldText(serviceCharge).isEmpty()) {
                parseDecimal(style.getFieldText(serviceCharge));
            }
        } catch (NumberFormatException e) {
            style.showFieldError(jTextField1);
            style.showMessageDialog(this,
                "Please enter valid numeric values",
                "Validation Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * Parse decimal string to BigDecimal
     * @param value The string value to parse
     * @return BigDecimal value or null if empty
     * @throws NumberFormatException if invalid format
     */
    private java.math.BigDecimal parseDecimal(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        // Remove commas and whitespace
        String cleaned = value.replaceAll("[,\\s]", "");
        return new java.math.BigDecimal(cleaned);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dateLabel;
    private javax.swing.JTextField formNo;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton loanCanvelButton;
    private javax.swing.JTextField no_of_months;
    private javax.swing.JTextArea remarks;
    private javax.swing.JButton saveLoanButton;
    private javax.swing.JTextField serviceCharge;
    // End of variables declaration//GEN-END:variables
}
