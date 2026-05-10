import re

with open('src/main/java/features/manageLedger.java', 'r') as f:
    content = f.read()

# Remove delete buttons and add empty ones just to not break updateButtonAndForm
content = re.sub(
    r'deletePaymentButton = new javax\.swing\.JButton\("Delete"\);.*?buttonPanel\.repaint\(\);\s*\}',
    '',
    content,
    flags=re.DOTALL
)

# Update the paymentTable mouse listener
payment_listener_new = """        paymentTable.addMouseListener(new java.awt.event.MouseAdapter() {
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
                        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                            manageLedger.this,
                            "Are you sure you want to delete this payment record?",
                            "Confirm Delete",
                            javax.swing.JOptionPane.YES_NO_OPTION
                        );
                        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                            try {
                                java.sql.Connection con = com.kelsz.esla.Database.getConnection();
                                java.sql.PreparedStatement ps = con.prepareStatement("DELETE FROM form_data WHERE id = ?");
                                ps.setInt(1, recordId);
                                ps.executeUpdate();
                                ps.close();
                                con.close();
                                
                                int selectedMemberIndex = memberList.getSelectedIndex();
                                if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                                    loadFormDataByLedger(ledgerId, memberIds.get(selectedMemberIndex));
                                } else {
                                    loadFormDataByLedger(ledgerId, null);
                                }
                                javax.swing.JOptionPane.showMessageDialog(manageLedger.this, "Payment deleted successfully!");
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
        });"""

content = re.sub(
    r'paymentTable\.addMouseListener\(new java\.awt\.event\.MouseAdapter\(\) \{.*?\n        \}\);',
    payment_listener_new,
    content,
    count=1,
    flags=re.DOTALL
)

# Update the loanTable mouse listener
loan_listener_new = """        loanTable.addMouseListener(new java.awt.event.MouseAdapter() {
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
                        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                            manageLedger.this,
                            "Are you sure you want to delete this loan record?",
                            "Confirm Delete",
                            javax.swing.JOptionPane.YES_NO_OPTION
                        );
                        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                            try {
                                java.sql.Connection con = com.kelsz.esla.Database.getConnection();
                                java.sql.PreparedStatement ps = con.prepareStatement("DELETE FROM loans WHERE id = ?");
                                ps.setInt(1, recordId);
                                ps.executeUpdate();
                                ps.close();
                                con.close();
                                
                                int selectedMemberIndex = memberList.getSelectedIndex();
                                if (selectedMemberIndex >= 0 && selectedMemberIndex < memberIds.size()) {
                                    loadLoansByLedger(ledgerId, memberIds.get(selectedMemberIndex));
                                } else {
                                    loadLoansByLedger(ledgerId, null);
                                }
                                javax.swing.JOptionPane.showMessageDialog(manageLedger.this, "Loan deleted successfully!");
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
        });"""

content = re.sub(
    r'loanTable\.addMouseListener\(new java\.awt\.event\.MouseAdapter\(\) \{.*?\n        \}\);',
    loan_listener_new,
    content,
    count=1,
    flags=re.DOTALL
)

# Update loadFormDataByLedger to add column widths and icons, remove inline editing logic
load_form_data_new = """    private void loadFormDataByLedger(int ledgerId, Integer memberId) {
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
            PreparedStatement ps;"""

content = re.sub(
    r'    private void loadFormDataByLedger\(int ledgerId, Integer memberId\) \{.*?try \{.*?Connection con = Database\.getConnection\(\);\s*String sql;\s*PreparedStatement ps;',
    load_form_data_new,
    content,
    count=1,
    flags=re.DOTALL
)

# Update loadLoansByLedger
load_loans_new = """    private void loadLoansByLedger(int ledgerId, Integer memberId) {
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
            PreparedStatement ps;"""

content = re.sub(
    r'    private void loadLoansByLedger\(int ledgerId, Integer memberId\) \{.*?try \{.*?Connection con = Database\.getConnection\(\);\s*String sql;\s*PreparedStatement ps;',
    load_loans_new,
    content,
    count=1,
    flags=re.DOTALL
)

# Replace while loops for inserting blank data into columns
content = content.replace(
    'rs.getString("remarks")',
    'rs.getString("remarks"), "", ""'
)

with open('src/main/java/features/manageLedger.java', 'w') as f:
    f.write(content)
