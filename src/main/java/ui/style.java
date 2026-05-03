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
    public static final int FIELD_HEIGHT = 40;
    public static final int SEARCH_WIDTH = 300;
    public static final int FILTER_WIDTH = 200;

    // 🎯 Button Style
    public static void applyButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Ubuntu", Font.BOLD, 16));
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 40, FIELD_HEIGHT));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                JButton b = (JButton) c;
                if (b.getModel().isPressed()) {
                    g2.setColor(PRIMARY.darker());
                } else if (b.getModel().isRollover()) {
                    g2.setColor(new Color(40, 80, 180));
                } else {
                    g2.setColor(b.getBackground());
                }
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 15, 15);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    // 🎯 Secondary Outlined Button Style
    public static void applySecondaryButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Ubuntu", Font.BOLD, 16));
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 40, FIELD_HEIGHT));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                JButton b = (JButton) c;
                if (b.getModel().isPressed()) {
                    g2.setColor(new Color(230, 230, 230));
                } else if (b.getModel().isRollover()) {
                    g2.setColor(new Color(245, 245, 250));
                } else {
                    g2.setColor(b.getBackground());
                }
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 15, 15);
                g2.setColor(PRIMARY);
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 15, 15);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    // 🎯 Back Button Style (text-only, no border)
    public static void applyBackButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Ubuntu", Font.BOLD, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(new Color(0, 0, 0));
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

    public static void applyCardPanel(JPanel panel) {
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 235), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
    }

    // 🏷️ Modern Label Style
    public static void applyModernLabel(JLabel label, boolean isTitle) {
        if (isTitle) {
            label.setFont(new Font("Ubuntu", Font.BOLD, 22));
            label.setForeground(PRIMARY);
        } else {
            label.setFont(new Font("Ubuntu", Font.BOLD, 14));
            label.setForeground(new Color(100, 100, 110));
        }
    }
    
    public static void applyTextField(JTextField field) {
        field.setFont(new Font("Ubuntu", Font.PLAIN, 16));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.DARK_GRAY);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, FIELD_HEIGHT));

        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 210), 1, true), // Softer rounded border
                BorderFactory.createEmptyBorder(0, 14, 0, 14) // Vertical padding handled by height/preferredSize
        ));
    }

    public static void applySearchField(JTextField field) {
        field.setFont(new Font("Ubuntu", Font.PLAIN, 16));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.DARK_GRAY);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, FIELD_HEIGHT));
        
        java.net.URL searchIconUrl = style.class.getResource("/images/search.png");
        final ImageIcon icon = (searchIconUrl != null) ? 
            new ImageIcon(new ImageIcon(searchIconUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)) : null;

        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY, 1, true),
            new javax.swing.border.AbstractBorder() {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    if (icon != null) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        icon.paintIcon(c, g2, x + 10, y + (height - icon.getIconHeight()) / 2);
                        g2.dispose();
                    }
                }
                @Override
                public Insets getBorderInsets(Component c) {
                    return new Insets(8, 35, 8, 12);
                }
            }
        ));
    }

    public static void applyComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Ubuntu", Font.PLAIN, 15));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.DARK_GRAY);
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, FIELD_HEIGHT));
        
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 210), 1, true),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));
        comboBox.setFocusable(false);
    }
       
    public static JPanel createSearchBar(JTextField field) {
        java.net.URL searchUrl = style.class.getResource("/images/search.png");
        Icon searchIcon = null;
        if (searchUrl != null) {
            searchIcon = new ImageIcon(new ImageIcon(searchUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
        }
        return createSearchBar(field, searchIcon);
    }

    public static JPanel createSearchBar(JTextField field, Icon searchIcon) {
        JPanel wrapper = new JPanel(new BorderLayout(8, 0));
        wrapper.setBackground(Color.WHITE);
        wrapper.setPreferredSize(new Dimension(300, FIELD_HEIGHT));

        JLabel iconLabel = new JLabel(searchIcon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        field.setFont(new Font("Ubuntu", Font.PLAIN, 15));
        field.setForeground(Color.DARK_GRAY);
        field.setCaretColor(Color.DARK_GRAY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 15));

        wrapper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 210, 220), 1, true),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));

        wrapper.add(iconLabel, BorderLayout.WEST);
        wrapper.add(field, BorderLayout.CENTER);

        return wrapper;
    }

    /**
     * 📏 Apply Standard Sizes
     * Sets the preferred size of search and filter components without creating new panels.
     */
    public static void applyStandardSizes(JComponent searchComp, JComponent filterComp) {
        if (searchComp != null) {
            searchComp.setPreferredSize(new Dimension(SEARCH_WIDTH, FIELD_HEIGHT));
        }
        if (filterComp != null) {
            filterComp.setPreferredSize(new Dimension(FILTER_WIDTH, FIELD_HEIGHT));
        }
    }
       
public static void applyListStyle(JList<?> list) {
        list.setFont(new Font("Ubuntu", Font.PLAIN, 15));
        list.setBackground(Color.WHITE);
        list.setForeground(Color.DARK_GRAY);
        list.setSelectionBackground(new Color(230, 240, 255));
        list.setSelectionForeground(PRIMARY);
        
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
                if (isSelected) {
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                }
                return label;
            }
        });
    }

    public static void applyScrollStyle(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(8, 0));
        verticalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 210);
                this.trackColor = Color.WHITE;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
                g2.dispose();
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        });
        
        JScrollBar horizontalBar = scrollPane.getHorizontalScrollBar();
        horizontalBar.setPreferredSize(new Dimension(0, 8));
        horizontalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 210);
                this.trackColor = Color.WHITE;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
                g2.dispose();
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        });
    }

    // 📅 JDateChooser Style
    public static void applyDateChooserStyle(com.toedter.calendar.JDateChooser chooser) {
        chooser.setFont(new Font("Ubuntu", Font.PLAIN, 15));
        chooser.setBackground(Color.WHITE);
        chooser.setBorder(BorderFactory.createEmptyBorder());
        chooser.setPreferredSize(new Dimension(chooser.getPreferredSize().width, FIELD_HEIGHT));

        // Style the text field inside
        JTextField editor = (JTextField) chooser.getDateEditor().getUiComponent();
        editor.setFont(new Font("Ubuntu", Font.PLAIN, 15));
        editor.setBackground(Color.WHITE);
        editor.setForeground(Color.DARK_GRAY);
        editor.setPreferredSize(new Dimension(editor.getPreferredSize().width, FIELD_HEIGHT));
        editor.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 210), 1, true),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));

        // Style the button
        JButton button = chooser.getCalendarButton();
        button.setBackground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
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

    // 🎨 Modern Undecorated Dialog Style
    public static void applyModernDialog(JDialog dialog, JPanel content, String title) {
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(200, 200, 210));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mainPanel.setOpaque(false);

        // Custom title bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Ubuntu", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY);

        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Ubuntu", Font.BOLD, 24));
        closeBtn.setForeground(new Color(150, 150, 150));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { closeBtn.setForeground(Color.RED); }
            public void mouseExited(java.awt.event.MouseEvent e) { closeBtn.setForeground(new Color(150, 150, 150)); }
        });
        closeBtn.addActionListener(e -> dialog.dispose());

        titleBar.add(titleLabel, BorderLayout.WEST);
        titleBar.add(closeBtn, BorderLayout.EAST);

        // Drag logic
        final Point[] mouseClickPoint = {null};
        titleBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                mouseClickPoint[0] = e.getPoint();
            }
        });
        titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                Point newPoint = e.getLocationOnScreen();
                dialog.setLocation(newPoint.x - mouseClickPoint[0].x, newPoint.y - mouseClickPoint[0].y);
            }
        });

        mainPanel.add(titleBar, BorderLayout.NORTH);

        // Content Wrapper
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        // Remove background from the provided content to blend in
        content.setOpaque(false);
        for (Component c : content.getComponents()) {
            if (c instanceof JPanel) {
                ((JPanel) c).setOpaque(false);
            }
        }
        
        contentWrapper.add(content, BorderLayout.CENTER);
        mainPanel.add(contentWrapper, BorderLayout.CENTER);

        dialog.setContentPane(mainPanel);
    }
}
