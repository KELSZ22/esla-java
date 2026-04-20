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

    // 🎯 Secondary Outlined Button Style
    public static void applySecondaryButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Ubuntu", Font.BOLD, 20));
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setBorder(BorderFactory.createLineBorder(PRIMARY, 2));
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

    // 🎨 Transparent TabPane Style
    public static void applyTransparentTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setFont(new Font("Ubuntu", Font.BOLD, 14));
        tabbedPane.setOpaque(false);
        
        // Tab area background - semi-transparent
        tabbedPane.setBackground(new Color(242, 242, 248, 200));
        
        // Custom UI for transparent tabs
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Rectangle tabRect = rects[tabIndex];
                boolean isSelected = tabbedPane.getSelectedIndex() == tabIndex;
                
                // Tab background with transparency
                if (isSelected) {
                    g2d.setColor(new Color(255, 255, 255, 240));
                    g2d.fillRoundRect(tabRect.x + 2, tabRect.y + 4, tabRect.width - 4, tabRect.height - 6, 8, 8);
                    
                    // Active tab border
                    g2d.setColor(new Color(21, 55, 143, 180));
                    g2d.drawRoundRect(tabRect.x + 2, tabRect.y + 4, tabRect.width - 4, tabRect.height - 6, 8, 8);
                } else {
                    g2d.setColor(new Color(255, 255, 255, 120));
                    g2d.fillRoundRect(tabRect.x + 2, tabRect.y + 4, tabRect.width - 4, tabRect.height - 6, 8, 8);
                    
                    // Inactive tab border
                    g2d.setColor(new Color(200, 200, 210, 100));
                    g2d.drawRoundRect(tabRect.x + 2, tabRect.y + 4, tabRect.width - 4, tabRect.height - 6, 8, 8);
                }
                
                // Tab text
                String title = tabbedPane.getTitleAt(tabIndex);
                g2d.setFont(new Font("Ubuntu", isSelected ? Font.BOLD : Font.PLAIN, 14));
                g2d.setColor(isSelected ? new Color(21, 55, 143) : new Color(100, 100, 110));
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(title);
                int textHeight = fm.getHeight();
                int textX = tabRect.x + (tabRect.width - textWidth) / 2;
                int textY = tabRect.y + (tabRect.height + textHeight) / 2 - fm.getDescent() + 2;
                
                g2d.drawString(title, textX, textY);
                g2d.dispose();
            }
            
            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent tab area background
                int tabHeight = calculateMaxTabHeight(tabPlacement);
                g2d.setColor(new Color(242, 242, 248, 150));
                g2d.fillRoundRect(0, 0, tabbedPane.getWidth(), tabHeight + 10, 12, 12);
                
                g2d.dispose();
                
                super.paintTabArea(g, tabPlacement, selectedIndex);
            }
            
            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                int width = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
                return width + 30; // Add padding
            }
            
            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                int height = super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
                return height + 10; // Add padding
            }
        });
        
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }
}
