package ui;

import java.math.BigDecimal;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class CollectedInterestForm extends javax.swing.JPanel {

    private int formId;
    private BigDecimal initialValue;

    public CollectedInterestForm(int formId, BigDecimal initialValue) {
        this.formId = formId;
        this.initialValue = initialValue;
        initComponents();
        applyStyling();
    }

    private void applyStyling() {
        style.applyFormPanel(this);
        style.applyModernLabel(jLabel1, true);
        style.applyModernLabel(jLabel2, false);
        style.applyTextField(collectedInterestField);
        style.applyPlaceholder(collectedInterestField, "Enter collected interest");
        style.applyButton(saveButton);
        style.applySecondaryButton(cancelButton);
        
        if (initialValue != null) {
            collectedInterestField.setText(initialValue.toString());
        }
    }

    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        collectedInterestField = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        jLabel1.setText("Edit Collected Interest");

        jLabel2.setText("Collected Interest");

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2)
                    .addComponent(collectedInterestField)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addGap(20, 20, 20)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(collectedInterestField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
        style.clearFieldError(collectedInterestField);

        String valStr = style.getFieldText(collectedInterestField);
        if (valStr.isEmpty()) {
            style.showFieldError(collectedInterestField);
            style.showMessageDialog(this, "Please enter a value", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BigDecimal newValue = new BigDecimal(valStr.replaceAll("[,\\s]", ""));
            services.MemberServiceChargeRefundFormService service = new services.MemberServiceChargeRefundFormService();
            boolean success = service.updateCollectedInterest(formId, newValue);
            
            if (success) {
                style.showMessageDialog(this, "Collected interest updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                SwingUtilities.getWindowAncestor(this).dispose();
            } else {
                style.showMessageDialog(this, "Failed to update collected interest.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            style.showFieldError(collectedInterestField);
            style.showMessageDialog(this, "Please enter a valid numeric value.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField collectedInterestField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton saveButton;
}
