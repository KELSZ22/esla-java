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
import ui.style;

/**
 *
 * @author kelsz-dev
 */
public class member extends javax.swing.JPanel {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMemberButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField memberSearchField;
    private javax.swing.JTable memberTable;
    private javax.swing.JComboBox<String> selectField;
    private javax.swing.JPanel memberSearchPanel;
    // End of variables declaration//GEN-END:variables

    // Store row-to-ID mapping for database operations
    private List<Integer> rowIds = new ArrayList<>();
    // Store all member data for filtering
    private List<Object[]> allMemberData = new ArrayList<>();
    // Track last edited cell to prevent duplicate confirmations
    private int lastEditedRow = -1;
    private int lastEditedCol = -1;

    // Custom cell editor for date column using JDateChooser
    class DateCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JDateChooser dateChooser;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private JTable table;

        public DateCellEditor() {
            dateChooser = new JDateChooser();
            dateChooser.setDateFormatString("yyyy-MM-dd");
            
            // Add listener to stop editing when date is selected
            dateChooser.addPropertyChangeListener("date", evt -> {
                if (table != null && dateChooser.getDate() != null) {
                    table.editingStopped(new javax.swing.event.ChangeEvent(this));
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            
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

    // Custom cell editor for member type column using JComboBox
    class TypeCellEditor extends AbstractCellEditor implements TableCellEditor {
        private javax.swing.JComboBox<String> comboBox;

        public TypeCellEditor() {
            comboBox = new javax.swing.JComboBox<>();
            comboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "channel 3", "resort", "executive", "consultant", "other" }));
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

    // Custom cell renderer to capitalize first letter of Member Type
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
     * Creates new form member
     */
    public member() {
        initComponents();
        setBackground(Color.WHITE);
        style.applyTableStyle(memberTable, 18, 18);
        style.applyScrollStyle(jScrollPane1);
        style.applyModernLabel(jLabel1, true);
        style.applyComboBox(selectField);
        style.applyButton(addMemberButton);
        style.applyTextField(memberSearchField);
        setupTable();
        populateMemberTypes();
        setupFilters();
        
        addMemberButton.addActionListener(this::AddMemberActionPerformed);

        // Add plus icon to AddMember button
        java.net.URL plusUrl = getClass().getResource("/images/plus.png");
        if (plusUrl != null) {
            ImageIcon plusIcon = new ImageIcon(
                    new ImageIcon(plusUrl).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
            );
            addMemberButton.setIcon(plusIcon);
            addMemberButton.setText(" Member");
        }

        // Apply standard styling and sizes
        style.applySearchField(memberSearchField);
        style.applyComboBox(selectField);
        style.applyStandardSizes(memberSearchField, selectField);
    }

    // =========================
    // POPULATE MEMBER TYPES
    // =========================
    private void populateMemberTypes() {
        try {
            Connection con = Database.getConnection();
            String sql = "SELECT DISTINCT member_type FROM members ORDER BY member_type";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            selectField.removeAllItems();
            selectField.addItem("All");

            while (rs.next()) {
                selectField.addItem(rs.getString("member_type"));
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
        memberSearchField.getDocument().addDocumentListener(new DocumentListener() {
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
        String searchText = memberSearchField.getText();
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
                // Delete = 7 (non-editable), others editable
                return column != 7;
            }
        };

        model.addColumn("Name");
        model.addColumn("Email");
        model.addColumn("Phone");
        model.addColumn("Address");
        model.addColumn("Member Type");
        model.addColumn("Premium");
        model.addColumn("Member Since");
        model.addColumn("Delete");

        try {
            Connection con = Database.getConnection();

            String sql = """
                SELECT id, name, email, phone, address, member_type, premium, member_since
                FROM members
                WHERE deleted_at IS NULL
                ORDER BY member_since DESC
            """;

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            // Clear existing ID mapping and data
            rowIds.clear();
            allMemberData.clear();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String address = rs.getString("address");
                String memberType = rs.getString("member_type");
                int premium = rs.getInt("premium");
                java.sql.Date sqlDate = rs.getDate("member_since");
                String memberSince = sqlDate != null ? sqlDate.toString() : "";

                // Store all data for filtering
                allMemberData.add(new Object[]{id, name, email, phone, address, memberType, premium, memberSince});

                // Apply filters
                boolean matchesSearch = searchText.isEmpty() ||
                    name.toLowerCase().contains(searchText.toLowerCase()) ||
                    email.toLowerCase().contains(searchText.toLowerCase()) ||
                    phone.toLowerCase().contains(searchText.toLowerCase()) ||
                    address.toLowerCase().contains(searchText.toLowerCase());

                boolean matchesType = memberTypeFilter.equals("All") ||
                    memberTypeFilter.equals(memberType);

                if (matchesSearch && matchesType) {
                    rowIds.add(id);
                    model.addRow(new Object[]{
                        name,
                        email,
                        phone,
                        address,
                        memberType,
                        premium,
                        memberSince,
                        "Delete"
                    });
                }
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading members: " + e.getMessage());
        }

        // Set column identifiers before setting model
        model.setColumnIdentifiers(new Object[]{"Name", "Email", "Phone", "Address", "Member Type", "Premium", "Member Since", ""});

        memberTable.setModel(model);

        // Set custom date editor for Member Since column (column 6)
        memberTable.getColumnModel().getColumn(6).setCellEditor(new DateCellEditor());

        // Set custom date renderer for Member Since column (column 6)
        memberTable.getColumnModel().getColumn(6).setCellRenderer(new DateCellRenderer());

        // Set custom type editor for Member Type column (column 4)
        memberTable.getColumnModel().getColumn(4).setCellEditor(new TypeCellEditor());

        // Set custom type renderer for Member Type column (column 4)
        memberTable.getColumnModel().getColumn(4).setCellRenderer(new TypeCellRenderer());

        // Minimize Delete column width and remove header label
        memberTable.getColumnModel().getColumn(7).setPreferredWidth(30);
        memberTable.getColumnModel().getColumn(7).setMinWidth(30);
        memberTable.getColumnModel().getColumn(7).setMaxWidth(30);

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
        memberTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setIcon(trashIcon);
                label.setText("");
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setOpaque(true);

                if (isSelected) {
                    label.setBackground(new Color(255, 200, 200));
                } else {
                    label.setBackground(table.getBackground());
                }

                return label;
            }
        });


        // Track hovered row for hover effect
        final int[] hoveredRow = {-1};
        memberTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = memberTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow[0]) {
                    hoveredRow[0] = row;
                    memberTable.repaint();
                }
            }
        });

        memberTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        
        // Prevent single-click editing - only allow double-click
        memberTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        
        model.addTableModelListener(e -> {

            if (e.getType() != javax.swing.event.TableModelEvent.UPDATE) return;

            int row = e.getFirstRow();
            int col = e.getColumn();

            if (col < 0) return;

            try {
                int id = rowIds.get(row);

                String columnName = switch (col) {
                    case 0 -> "name";
                    case 1 -> "email";
                    case 2 -> "phone";
                    case 3 -> "address";
                    case 4 -> "member_type";
                    case 5 -> "premium";
                    case 6 -> "member_since";
                    default -> null;
                };

                if (columnName == null) return;

                // Prevent duplicate confirmations for the same cell
                if (row == lastEditedRow && col == lastEditedCol) {
                    lastEditedRow = -1;
                    lastEditedCol = -1;
                    return;
                }

                // Mark this cell as being edited before showing confirmation
                lastEditedRow = row;
                lastEditedCol = col;

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
                    lastEditedRow = -1;
                    lastEditedCol = -1;
                    return;
                }

                Connection con = Database.getConnection();

                String sql = "UPDATE members SET " + columnName + " = ? WHERE id = ?";
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

        // UI tweaks
        memberTable.setRowHeight(35);
        memberTable.getTableHeader().setReorderingAllowed(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        memberTable = new javax.swing.JTable();
        selectField = new javax.swing.JComboBox<>();
        addMemberButton = new javax.swing.JButton();
        memberSearchField = new javax.swing.JTextField();
        memberSearchPanel = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel1.setText("MEMBER");

        memberTable.setModel(new javax.swing.table.DefaultTableModel(
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
        memberTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                memberTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(memberTable);

        selectField.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        addMemberButton.setText("Member");

        memberSearchField.addActionListener(this::memberSearchFieldActionPerformed);

        memberSearchPanel.setBackground(new java.awt.Color(255, 255, 255));

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
                        .addComponent(memberSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectField, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 620, Short.MAX_VALUE)
                        .addComponent(addMemberButton, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(memberSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(selectField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(addMemberButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                .addGap(31, 31, 31))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void memberTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_memberTableMouseClicked

        int row = memberTable.rowAtPoint(evt.getPoint());
        int col = memberTable.columnAtPoint(evt.getPoint());

        if (row < 0 || col < 0) return;

        // Handle Delete column click
        if (col == 7) {
            int id = rowIds.get(row);
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this member?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Connection con = Database.getConnection();
                    String sql = "UPDATE members SET deleted_at = NOW() WHERE id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    ps.close();
                    con.close();

                    // Refresh table
                    setupTable();
                    JOptionPane.showMessageDialog(null, "Member deleted successfully!");
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error deleting member: " + e.getMessage());
                }
            }
            return;
        }

        // Prevent editing Delete column
        if (col == 7) return;

        // Only trigger edit on double-click
        if (evt.getClickCount() == 2) {
            memberTable.editCellAt(row, col);
            memberTable.getEditorComponent().requestFocus();
        }

    }//GEN-LAST:event_memberTableMouseClicked

    private void memberSearchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberSearchFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_memberSearchFieldActionPerformed

    private void AddMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddMemberActionPerformed
        // Get the parent frame
        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        
        // Create the MemberForm panel
        ui.MemberForm memberFormPanel = new ui.MemberForm();
        
        // Wrap it in a modern dialog
        javax.swing.JDialog dialog = new javax.swing.JDialog(parentFrame, "Create Member", true);
        style.applyModernDialog(dialog, memberFormPanel, "Create Member");
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
        
        // Refresh the table after the modal closes
        setupTable();
        populateMemberTypes();
    }//GEN-LAST:event_AddMemberActionPerformed

}
