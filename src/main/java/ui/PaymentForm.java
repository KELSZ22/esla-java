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
public class PaymentForm extends javax.swing.JDialog {

    private int ledgerId;
    private int memberId;
    private String ledgerType;
    private boolean isEditMode = false;
    private java.math.BigDecimal scheduledPaymentForDate;
    private java.math.BigDecimal shouldBePaidForDate;

    /**
     * Creates new form PaymentForm as a modal dialog
     * @param parent The parent frame
     * @param ledgerId The ledger ID
     * @param memberId The member ID
     * @param ledgerType The ledger type
     */
    public PaymentForm(java.awt.Frame parent, int ledgerId, int memberId, String ledgerType) {
        super(parent, "Create Payment", true);
        this.ledgerId = ledgerId;
        this.memberId = memberId;
        this.ledgerType = ledgerType;
        initComponents();
        applyStyling();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        // Auto-fill date with next cutoff date
        autoFillCutoffDate();
        
        // Auto-fill premium from member data
        autoFillPremium();
        
        // Add date change listener for real-time computation
        addDateChangeListener();
        
        // Initial computation for prospective date
        fetchProspectivePayments();
        
        // Disable scheduled payment and should be paid fields
        scheduledPayment.setEnabled(false);
        shouldBePaid.setEnabled(false);
        
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Creates new form PaymentForm as a modal dialog (for backward compatibility)
     * @param parent The parent frame
     * @deprecated Use the constructor with ledgerId, memberId, and ledgerType parameters
     */
    @Deprecated
    public PaymentForm(java.awt.Frame parent) {
        super(parent, "Create Payment", true);
        initComponents();
        applyStyling();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    private void applyStyling() {
        // Apply dialog content pane styling
        getContentPane().setBackground(java.awt.Color.WHITE);

        // Apply text field styling
        style.applyTextField(scheduledPayment);
        style.applyTextField(shouldBePaid);
        style.applyTextField(actualPayment);
        style.applyTextField(premium);

        // Apply button styling
        style.applyButton(saveButton);
        style.applySecondaryButton(Cancel);

        // Style remarks text area
        remarks.setFont(new java.awt.Font("Ubuntu", java.awt.Font.PLAIN, 14));
        remarks.setBackground(java.awt.Color.WHITE);
        remarks.setForeground(new java.awt.Color(40, 40, 40));
        remarks.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(style.PRIMARY, 1, true),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        remarks.setLineWrap(true);
        remarks.setWrapStyleWord(true);

        // Style date chooser
        date.setFont(new java.awt.Font("Ubuntu", java.awt.Font.PLAIN, 14));
        date.setBackground(java.awt.Color.WHITE);
    }

    /**
     * Auto-fill date with next cutoff date (15, 31, or last day of month)
     */
    private void autoFillCutoffDate() {
        LocalDate today = LocalDate.now();
        LocalDate nextCutoff = calculateNextCutoffDate(today);
        date.setDate(java.sql.Date.valueOf(nextCutoff));
    }

    /**
     * Calculate the next cutoff date (15, 31, or last day of month)
     * @param fromDate The date to calculate from
     * @return The next cutoff date
     */
    private LocalDate calculateNextCutoffDate(LocalDate fromDate) {
        int currentDay = fromDate.getDayOfMonth();
        int lastDayOfMonth = fromDate.lengthOfMonth();
        
        // Cutoff dates are 15 and last day of month (which could be 30, 31, 28, or 29)
        int cutoff1 = 15;
        int cutoff2 = lastDayOfMonth;
        
        // If today is before 15, next cutoff is 15
        if (currentDay < cutoff1) {
            return fromDate.withDayOfMonth(cutoff1);
        }
        // If today is before last day of month, next cutoff is last day
        else if (currentDay < cutoff2) {
            return fromDate.withDayOfMonth(cutoff2);
        }
        // If today is after last day of month, next cutoff is 15 of next month
        else {
            LocalDate nextMonth = fromDate.plusMonths(1);
            return nextMonth.withDayOfMonth(cutoff1);
        }
    }

    /**
     * Auto-fill premium from member data
     */
    private void autoFillPremium() {
        try {
            services.FormDataService formDataService = new services.FormDataService();
            java.math.BigDecimal memberPremium = formDataService.getMemberPremium(memberId);
            if (memberPremium != null) {
                premium.setText(formatCurrency(memberPremium));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add PropertyChangeListener to date chooser for real-time computation
     */
    private void addDateChangeListener() {
        date.addPropertyChangeListener("date", evt -> {
            if (!isEditMode) {
                fetchProspectivePayments();
            }
        });
    }

    /**
     * Fetch prospective payments from server for the selected date
     * Updates scheduledPaymentForDate and shouldBePaidForDate state variables
     */
    private void fetchProspectivePayments() {
        try {
            java.util.Date selectedDate = date.getDate();
            if (selectedDate == null) {
                scheduledPaymentForDate = null;
                shouldBePaidForDate = null;
                updatePaymentFieldsDisplay();
                return;
            }

            LocalDate paymentDate = selectedDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

            services.FormDataService formDataService = new services.FormDataService();
            services.FormDataService.ProspectivePayment prospective =
                formDataService.getProspectivePaymentsForDate(memberId, ledgerType, paymentDate);

            System.out.println("DEBUG: memberId=" + memberId + ", ledgerType=" + ledgerType + ", paymentDate=" + paymentDate);
            System.out.println("DEBUG: scheduledPayment=" + prospective.scheduledPayment);
            System.out.println("DEBUG: shouldBePaid=" + prospective.shouldBePaid);

            scheduledPaymentForDate = prospective.scheduledPayment;
            shouldBePaidForDate = prospective.shouldBePaid;

            updatePaymentFieldsDisplay();
        } catch (Exception e) {
            e.printStackTrace();
            scheduledPaymentForDate = null;
            shouldBePaidForDate = null;
            updatePaymentFieldsDisplay();
        }
    }

    /**
     * Update payment fields display with priority order
     * 
     * Scheduled Payment Priority:
     * 1. If editing: uses formData.scheduled_payment
     * 2. If creating with date: uses scheduledPaymentForDate (live server value)
     * 3. Fallback: uses nextScheduledPayment prop (from parent component)
     * 4. Default: '0.00'
     * 
     * Should Be Paid Priority:
     * 1. If editing: uses formData.should_be_paid
     * 2. If creating with date: uses shouldBePaidForDate (live server value)
     * 3. Default: '—'
     */
    private void updatePaymentFieldsDisplay() {
        // Scheduled Payment display logic
        if (isEditMode) {
            // In edit mode, keep existing value (would be set from formData)
            // For now, keep current text
        } else {
            // In create mode, use prospective value
            if (scheduledPaymentForDate != null) {
                scheduledPayment.setText(formatCurrency(scheduledPaymentForDate));
            } else {
                scheduledPayment.setText("0.00");
            }
        }

        // Should Be Paid display logic
        if (isEditMode) {
            // In edit mode, keep existing value (would be set from formData)
            // For now, keep current text
        } else {
            // In create mode, use prospective value
            if (shouldBePaidForDate != null) {
                shouldBePaid.setText(formatCurrency(shouldBePaidForDate));
            } else {
                shouldBePaid.setText("—");
            }
        }
    }

    /**
     * Format BigDecimal as currency string
     * @param value The value to format
     * @return Formatted currency string
     */
    private String formatCurrency(java.math.BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        java.text.DecimalFormat currencyFormat = new java.text.DecimalFormat("#,##0.00");
        return currencyFormat.format(value);
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
        jLabel3 = new javax.swing.JLabel();
        date = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        scheduledPayment = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        shouldBePaid = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        actualPayment = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        premium = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        remarks = new javax.swing.JTextArea();
        jLabel8 = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        Cancel = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        jLabel1.setText("Create Payment");

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel3.setText("Date:");

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel4.setText("Scheduled Payment");

        scheduledPayment.addActionListener(this::scheduledPaymentActionPerformed);

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel6.setText("Should be paid");

        shouldBePaid.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel7.setText("Actual payment");

        actualPayment.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel5.setText("Premium");

        premium.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        premium.addActionListener(this::premiumActionPerformed);

        remarks.setColumns(20);
        remarks.setRows(5);
        jScrollPane1.setViewportView(remarks);

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        jLabel8.setText("Remarks");

        saveButton.setText("Save");
        saveButton.addActionListener(this::saveButtonActionPerformed);

        Cancel.setText("Cancel");
        Cancel.addActionListener(this::CancelActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(date, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(shouldBePaid, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7)
                                    .addComponent(actualPayment, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                                    .addComponent(scheduledPayment))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(premium, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8))
                                .addGap(0, 353, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(Cancel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(saveButton)))))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(9, 9, 9)
                                .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(7, 7, 7)
                                .addComponent(scheduledPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(shouldBePaid, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(actualPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(premium, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Cancel)
                    .addComponent(saveButton))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void scheduledPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scheduledPaymentActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_scheduledPaymentActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // Validate form inputs
        if (!validateForm()) {
            return;
        }

        try {
            // Parse form inputs
            java.util.Date selectedDate = date.getDate();
            if (selectedDate == null) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Please select a date",
                    "Validation Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate paymentDate = selectedDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

            java.math.BigDecimal scheduledPayment = parseDecimal(this.scheduledPayment.getText());
            java.math.BigDecimal premium = parseDecimal(this.premium.getText());
            java.math.BigDecimal actualPayment = parseDecimal(this.actualPayment.getText());
            String remarks = this.remarks.getText();

            // Create payment using PaymentService
            services.PaymentService paymentService = new services.PaymentService();
            int recordId = paymentService.processPayment(
                ledgerId, memberId, ledgerType, paymentDate,
                scheduledPayment, premium, actualPayment, remarks
            );

            if (recordId > 0) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Payment created successfully!",
                    "Success",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Failed to create payment. Please try again.",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                "Error creating payment: " + e.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    /**
     * Validate form inputs
     * @return true if valid, false otherwise
     */
    private boolean validateForm() {
        if (date.getDate() == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please select a date",
                "Validation Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            if (!scheduledPayment.getText().trim().isEmpty()) {
                parseDecimal(scheduledPayment.getText());
            }
            if (!premium.getText().trim().isEmpty()) {
                parseDecimal(premium.getText());
            }
            if (!actualPayment.getText().trim().isEmpty()) {
                parseDecimal(actualPayment.getText());
            }
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please enter valid numeric values for payment fields",
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

    private void premiumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_premiumActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_premiumActionPerformed

    private void CancelActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Cancel;
    private javax.swing.JTextField actualPayment;
    private com.toedter.calendar.JDateChooser date;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField premium;
    private javax.swing.JTextArea remarks;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextField scheduledPayment;
    private javax.swing.JTextField shouldBePaid;
    // End of variables declaration//GEN-END:variables
}
