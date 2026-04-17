/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

/**
 *
 * @author kelsz-dev
 */
import javax.swing.*;
import java.awt.*;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
public class style {
    
  public static final Color PRIMARY = new Color(21, 55, 143);
    public static final Color BACKGROUND = new Color(242, 242, 248);

    // 🎯 Button Style
    public static void applyButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Ubuntu", Font.BOLD, 20));
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
public static void applyNavButton(JButton btn) {
    btn.setFocusPainted(false);
    btn.setFont(new Font("Ubuntu", Font.BOLD, 16));

    btn.setContentAreaFilled(false);
    btn.setOpaque(true); // ✅ important
    btn.setBorderPainted(false);

    btn.setForeground(new Color(120, 120, 120)); // default gray
    btn.setBackground(new Color(242, 242, 248)); // optional but stabilizes rendering
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
}
    
public static void setActiveNav(JButton active, JButton... buttons) {

    for (JButton btn : buttons) {
        btn.setForeground(new Color(120, 120, 120));
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
    }

    // 🔥 FORCE ACTIVE STATE
    active.setForeground(new Color(33, 150, 243));
    active.setFont(active.getFont().deriveFont(Font.BOLD));

    // 💡 FORCE UI UPDATE (THIS IS WHAT YOU WERE MISSING)
    active.setUI(new javax.swing.plaf.basic.BasicButtonUI());
    active.repaint();
    active.revalidate();
}
    

    // 🧱 Panel Style
    public static void applyPanel(JPanel panel) {
        panel.setBackground(BACKGROUND);
    }
    
       public static void applyTextField(JTextField field) {
        field.setFont(new Font("Ubuntu", Font.PLAIN, 16));

        field.setBackground(Color.WHITE);
        field.setForeground(Color.DARK_GRAY);

        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY, 1, true), // 🔥 rounded border
                BorderFactory.createEmptyBorder(8, 12, 8, 12) // padding
        ));
    }

    public static void applyComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Ubuntu", Font.PLAIN, 15));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.DARK_GRAY);
        comboBox.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        comboBox.setFocusable(false);
    }
       
       public static JPanel createSearchBar(JTextField field, Icon searchIcon) {

    JPanel wrapper = new JPanel(new BorderLayout(8, 0));
    wrapper.setBackground(Color.WHITE);
    wrapper.setPreferredSize(new Dimension(298, 40)); // 💥 fixed width 298px

    JLabel iconLabel = new JLabel(searchIcon);
    iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

    field.setFont(new Font("Ubuntu", Font.PLAIN, 15));
    field.setForeground(Color.DARK_GRAY);
    field.setCaretColor(Color.DARK_GRAY);
    field.setBackground(Color.WHITE);

    field.setPreferredSize(new Dimension(248, 30)); // 💥 adjusted for fixed width

    field.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 10));

    wrapper.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY, 1, true),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
    ));

    wrapper.add(iconLabel, BorderLayout.WEST);
    wrapper.add(field, BorderLayout.CENTER);

    return wrapper;
}
       
public static void applyTableStyle(JTable table, int fontSize, int headerFontSize) {

    // 🧠 Basic table settings
    table.setRowHeight(fontSize + 20);
    table.setFont(new Font("Ubuntu", Font.PLAIN, fontSize));
    table.setForeground(new Color(40, 40, 40));
    table.setGridColor(new Color(230, 230, 235));

    table.setShowHorizontalLines(true);
    table.setShowVerticalLines(false);
    table.setIntercellSpacing(new Dimension(0, 0));
    table.setSelectionBackground(new Color(220, 235, 255));
    table.setSelectionForeground(Color.BLACK);
    table.setFillsViewportHeight(true);

    // 🎯 Header styling
    JTableHeader header = table.getTableHeader();

    header.setFont(new Font("Ubuntu", Font.BOLD, headerFontSize));
    header.setBackground(new Color(242, 242, 248));
    header.setForeground(new Color(60, 60, 60));
    header.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

    // ? FORCE ALL CAPS RENDERING
    header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel label = new JLabel(String.valueOf(value).toUpperCase());
            label.setFont(new Font("Ubuntu", Font.BOLD, headerFontSize));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
            label.setOpaque(true);
            label.setBackground(new Color(242, 242, 248));
            label.setForeground(new Color(60, 60, 60));

            return label;
        }
    });
}
}
