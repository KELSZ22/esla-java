/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package features;

import com.kelsz.esla.Database;
import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import ui.LedgerForm;
import ui.style;

/**
 *
 * @author kelsz-dev
 */
public class ledger extends javax.swing.JPanel {

    // Store row-to-ID mapping for database operations
    private List<Integer> rowIds = new ArrayList<>();
    // Store all ledger data for filtering
    private List<Object[]> allLedgerData = new ArrayList<>();
    // Reference to manageLedger panel for layout switching
    private manageLedger currentManagePanel;
    // Reference to LedgerForm panel for adding new ledger entries
    private LedgerForm currentLedgerForm;

    // Custom cell editor for date column using JDateChooser
    class DateCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JDateChooser dateChooser;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        public DateCellEditor() {
            dateChooser = new JDateChooser();
            dateChooser.setDateFormatString("yyyy-MM-dd");
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value != null && !value.toString().isEmpty()) {
                try {
                    Date date = dateFormat.parse(value.toString());
                    dateChooser.setDate(date);
                } catch (Exception e) {
                    dateChooser.setDate(null);
                }
            } else {
                dateChooser.setDate(null);
            }
            return dateChooser;
        }

        @Override
        public Object getCellEditorValue() {
            Date date = dateChooser.getDate();
            if (date != null) {
                return dateFormat.format(date);
            }
            return "";
        }
    }

    // Custom cell renderer to display dates in "MMMM dd, yyyy" format
    class DateCellRenderer extends DefaultTableCellRenderer {
        private SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy");
        private SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null && !value.toString().isEmpty()) {
                try {
                    Date date = dbFormat.parse(value.toString());
                    setText(displayFormat.format(date));
                } catch (Exception e) {
                    setText(value.toString());
                }
            } else {
                setText("");
            }

            return this;
        }
    }

    // Custom cell editor for type column using JComboBox
    class TypeCellEditor extends AbstractCellEditor implements TableCellEditor {
        private javax.swing.JComboBox<String> comboBox;

        public TypeCellEditor() {
            comboBox = new javax.swing.JComboBox<>();
            comboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "income", "expense", "loan", "payment" }));
            style.applyComboBox(comboBox);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value != null) {
                comboBox.setSelectedItem(value.toString());
            }
            return comboBox;
        }

        @Override
        public Object getCellEditorValue() {
            return comboBox.getSelectedItem();
        }
    }

    // Custom cell renderer to capitalize first letter of Type
    class TypeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null && !value.toString().isEmpty()) {
                String text = value.toString();
                if (text.length() > 0) {
                    text = text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
                }
                setText(text);
            } else {
                setText("");
            }

            return this;
        }
    }

    /**
     * Creates new form ledger
     */
    public ledger() {
    initComponents();
    setBackground(Color.WHITE);
    style.applyTableStyle(ledgerTable, 18, 18);
    style.applyScrollStyle(jScrollPane1);
    style.applyModernLabel(jLabel1, true);
    style.applyComboBox(selectField);
    style.applyButton(AddLedger);
    setupTable();
    populateMemberTypes();
    setupFilters();
    
    AddLedger.addActionListener(this::AddLedgerActionPerformed);

    // Add plus icon to AddLedger button
    java.net.URL plusUrl = getClass().getResource("/images/plus.png");
    if (plusUrl != null) {
        ImageIcon plusIcon = new ImageIcon(
                new ImageIcon(plusUrl).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
        );
        AddLedger.setIcon(plusIcon);
        AddLedger.setText(" Ledger");
    }

    // Apply standard styling and sizes
    style.applySearchField(ledgerSearchField);
    style.applyComboBox(selectField);
    style.applyStandardSizes(ledgerSearchField, selectField);
}

    // =========================
    // POPULATE MEMBER TYPES
    // =========================
    private void populateMemberTypes() {
        try {
            Connection con = Database.getConnection();
            String sql = "SELECT DISTINCT type FROM ledgers ORDER BY type";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            selectField.removeAllItems();
            selectField.addItem("All");

            while (rs.next()) {
                selectField.addItem(rs.getString("type"));
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // SETUP FILTERS
    // =========================
    private void setupFilters() {
        // Search field filter
        ledgerSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        // Member type filter
        selectField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
    }

    // =========================
    // APPLY FILTERS
    // =========================
    private void applyFilters() {
        String searchText = ledgerSearchField.getText();
        String memberType = (String) selectField.getSelectedItem();
        if (memberType == null) {
            memberType = "All";
        }
        setupTable(searchText, memberType);
    }
    
    
    // =========================
    // TABLE DATA SETUP
    // =========================
private void setupTable() {
    setupTable("", "All");
}

private void setupTable(String searchText, String memberTypeFilter) {

 DefaultTableModel model = new DefaultTableModel() {
    @Override
    public boolean isCellEditable(int row, int column) {
        // Manage = 3, Delete = 4 (non-editable), others editable
        return column != 3 && column != 4;
    }
};

   model.addColumn("Description");
model.addColumn("Type");
model.addColumn("Date");
model.addColumn("Manage");
model.addColumn("Delete");

    try {
        Connection con = Database.getConnection();

        String sql = """
            SELECT id, description, type, date
            FROM ledgers
            ORDER BY date DESC
        """;

        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        // Clear existing ID mapping and data
        rowIds.clear();
        allLedgerData.clear();

        while (rs.next()) {
            int id = rs.getInt("id");
            String description = rs.getString("description");
            String type = rs.getString("type");
            java.sql.Date sqlDate = rs.getDate("date");
            String date = sqlDate != null ? sqlDate.toString() : "";

            // Store all data for filtering
            allLedgerData.add(new Object[]{id, description, type, date});

            // Apply filters
            boolean matchesSearch = searchText.isEmpty() ||
                description.toLowerCase().contains(searchText.toLowerCase()) ||
                type.toLowerCase().contains(searchText.toLowerCase());

            boolean matchesType = memberTypeFilter.equals("All") ||
                memberTypeFilter.equals(type);

            if (matchesSearch && matchesType) {
                rowIds.add(id);
                model.addRow(new Object[]{
                    description,
                    type,
                    date,
                    "Manage",
                    "Delete"
                });
            }
        }

        rs.close();
        ps.close();
        con.close();

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error loading ledger: " + e.getMessage());
    }

    // Set column identifiers before setting model
    model.setColumnIdentifiers(new Object[]{"Description", "Type", "Date", "", ""});

    ledgerTable.setModel(model);

    // Set custom date editor for Date column (column 2)
    ledgerTable.getColumnModel().getColumn(2).setCellEditor(new DateCellEditor());

    // Set custom date renderer for Date column (column 2)
    ledgerTable.getColumnModel().getColumn(2).setCellRenderer(new DateCellRenderer());

    // Set custom type editor for Type column (column 1)
    ledgerTable.getColumnModel().getColumn(1).setCellEditor(new TypeCellEditor());

    // Set custom type renderer for Type column (column 1)
    ledgerTable.getColumnModel().getColumn(1).setCellRenderer(new TypeCellRenderer());

    // Load manage icon
    java.net.URL settingsUrl = getClass().getResource("/images/file-text.png");
    ImageIcon manageIcon;
    if (settingsUrl != null) {
        manageIcon = new ImageIcon(
                new ImageIcon(settingsUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)
        );
    } else {
        manageIcon = null;
    }

    // Custom renderer for Manage column
    ledgerTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setIcon(manageIcon);
            label.setText("");
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(new Color(200, 220, 255));
            } else {
                label.setBackground(table.getBackground());
            }

            return label;
        }
    });

    // Minimize Manage column width
    ledgerTable.getColumnModel().getColumn(3).setPreferredWidth(30);
    ledgerTable.getColumnModel().getColumn(3).setMinWidth(30);
    ledgerTable.getColumnModel().getColumn(3).setMaxWidth(30);

    // Load trash icon
    java.net.URL trashUrl = getClass().getResource("/images/trash-2.png");
    ImageIcon trashIcon;
    if (trashUrl != null) {
        trashIcon = new ImageIcon(
                new ImageIcon(trashUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)
        );
    } else {
        trashIcon = null;
    }

    // Custom renderer for Delete column with hover effect
    ledgerTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setIcon(trashIcon);
            label.setText("");
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setOpaque(true);

            // Hover effect - change background on mouse over
            if (isSelected) {
                label.setBackground(new Color(255, 200, 200));
            } else {
                label.setBackground(table.getBackground());
            }

            return label;
        }
    });

    // Minimize Delete column width and remove header label
    ledgerTable.getColumnModel().getColumn(4).setPreferredWidth(30);
    ledgerTable.getColumnModel().getColumn(4).setMinWidth(30);
    ledgerTable.getColumnModel().getColumn(4).setMaxWidth(30);

    // Track hovered row for hover effect
    final int[] hoveredRow = {-1};
    ledgerTable.addMouseMotionListener(new MouseAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
            int row = ledgerTable.rowAtPoint(e.getPoint());
            if (row != hoveredRow[0]) {
                hoveredRow[0] = row;
                ledgerTable.repaint();
            }
        }
    });

    ledgerTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    
    // Prevent single-click editing - only allow double-click
    ledgerTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
    
    model.addTableModelListener(e -> {

    if (e.getType() != javax.swing.event.TableModelEvent.UPDATE) return;

    int row = e.getFirstRow();
    int col = e.getColumn();

    if (col < 0) return;

    try {
        int id = rowIds.get(row);

        String description = model.getValueAt(row, 0).toString();
        String type = model.getValueAt(row, 1).toString();
        String date = model.getValueAt(row, 2).toString();

        String columnName = switch (col) {
            case 0 -> "description";
            case 1 -> "type";
            case 2 -> "date";
            default -> null;
        };

        if (columnName == null) return;

        // Show confirmation dialog before updating
        Object newValue = model.getValueAt(row, col);
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
            "Are you sure you want to update " + columnName + " to: " + (newValue != null ? newValue.toString() : "") + "?",
            "Confirm Update",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            // Reload data to revert the change
            applyFilters();
            return;
        }

        Connection con = Database.getConnection();

        String sql = "UPDATE ledgers SET " + columnName + " = ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setObject(1, newValue);
        ps.setInt(2, id);

        ps.executeUpdate();

        ps.close();
        con.close();

        System.out.println("Auto-saved row ID: " + id);

    } catch (Exception ex) {
        ex.printStackTrace();
    }
});

    // UI tweaks ✨
    ledgerTable.setRowHeight(35);
    ledgerTable.getTableHeader().setReorderingAllowed(false);
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
        jScrollPane1 = new javax.swing.JScrollPane();
        ledgerTable = new javax.swing.JTable();
        ledgerSearchPanel = new javax.swing.JPanel();
        ledgerSearchField = new javax.swing.JTextField();
        selectField = new javax.swing.JComboBox<>();
        AddLedger = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel1.setText("LEDGER");

        ledgerTable.setModel(new javax.swing.table.DefaultTableModel(
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
        ledgerTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ledgerTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(ledgerTable);

        ledgerSearchField.addActionListener(this::ledgerSearchFieldActionPerformed);

        selectField.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        ledgerSearchPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout ledgerSearchPanelLayout = new javax.swing.GroupLayout(ledgerSearchPanel);
        ledgerSearchPanel.setLayout(ledgerSearchPanelLayout);
        ledgerSearchPanelLayout.setHorizontalGroup(
            ledgerSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ledgerSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ledgerSearchField, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectField, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        ledgerSearchPanelLayout.setVerticalGroup(
            ledgerSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ledgerSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ledgerSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ledgerSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        AddLedger.setText("Ledger");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ledgerSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(AddLedger, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1252, Short.MAX_VALUE))
                .addGap(40, 40, 40))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addGap(59, 59, 59)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ledgerSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AddLedger, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
                .addGap(31, 31, 31))
        );
    } // </editor-fold>//GEN-END:initComponents

    private void ledgerTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ledgerTableMouseClicked

    int row = ledgerTable.rowAtPoint(evt.getPoint());
    int col = ledgerTable.columnAtPoint(evt.getPoint());

    if (row < 0 || col < 0) return;

    // Handle Manage column click
    if (col == 3) {
        int id = rowIds.get(row);
        // Open manageLedger panel with selected ledger data
        currentManagePanel = new manageLedger();
        
        // Load ledger data for this entry
        Object[] ledgerData = allLedgerData.stream()
            .filter(data -> (int)data[0] == id)
            .findFirst()
            .orElse(null);
        
        if (ledgerData != null) {
            // Load actual payment and loan data for this ledger
            String ledgerType = (String) ledgerData[2]; // type is at index 2
            currentManagePanel.loadLedgerData(id, ledgerType);
        }
        
        // Set back button callback
        currentManagePanel.setBackButtonCallback(() -> {
            // Show ledger components
            jLabel1.setVisible(true);
            ledgerSearchPanel.setVisible(true);
            selectField.setVisible(true);
            jScrollPane1.setVisible(true);
            AddLedger.setVisible(true);
            // Remove manageLedger panel
            this.remove(currentManagePanel);
            // Restore original GroupLayout layout
            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(ledgerSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(AddLedger, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(40, 40, 40))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(25, 25, 25)
                    .addComponent(jLabel1)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(59, 59, 59)
                            .addComponent(ledgerSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(AddLedger)
                            .addGap(14, 14, 14)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                    .addGap(31, 31, 31))
            );
            this.revalidate();
            this.repaint();
        });
        
        // Hide ledger components and show manageLedger
        jLabel1.setVisible(false);
        ledgerSearchPanel.setVisible(false);
        selectField.setVisible(false);
        jScrollPane1.setVisible(false);
        AddLedger.setVisible(false);

        // Add manageLedger panel to this panel using BorderLayout
        this.setLayout(new BorderLayout());
        this.add(currentManagePanel, BorderLayout.CENTER);
        currentManagePanel.setVisible(true);

        return;
    }

    // Handle Delete column click
    if (col == 4) {
        int id = rowIds.get(row);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this ledger entry?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection con = Database.getConnection();
                String sql = "DELETE FROM ledgers WHERE id = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, id);
                ps.executeUpdate();
                ps.close();
                con.close();

                // Refresh table
                setupTable();
                JOptionPane.showMessageDialog(null, "Entry deleted successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error deleting entry: " + e.getMessage());
            }
        }
        return;
    }

    // 🚫 prevent editing Delete and Manage columns
    if (col == 3 || col == 4) return;

    // Only trigger edit on double-click
    if (evt.getClickCount() == 2) {
        ledgerTable.editCellAt(row, col);
        ledgerTable.getEditorComponent().requestFocus();
    }

        }//GEN-LAST:event_ledgerTableMouseClicked

    private void ledgerSearchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ledgerSearchFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ledgerSearchFieldActionPerformed

    private void AddLedgerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddLedgerActionPerformed
        // Create LedgerForm panel
        currentLedgerForm = new LedgerForm();

        // Create modern dialog
        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        javax.swing.JDialog dialog = new javax.swing.JDialog(parentFrame, "Add New Ledger", true);
        style.applyModernDialog(dialog, currentLedgerForm, "Add New Ledger");
        dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);

        // Set callback to refresh table when dialog closes
        currentLedgerForm.setCallback(() -> {
            dialog.dispose();
            // Refresh table and member types
            setupTable();
            populateMemberTypes();
        });

        dialog.setVisible(true);
    }//GEN-LAST:event_AddLedgerActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddLedger;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField ledgerSearchField;
    private javax.swing.JPanel ledgerSearchPanel;
    private javax.swing.JTable ledgerTable;
    private javax.swing.JComboBox<String> selectField;
    // End of variables declaration//GEN-END:variables

  
}
