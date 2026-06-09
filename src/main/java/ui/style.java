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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
public class style {
    
    public static final Color PRIMARY = new Color(21, 55, 143);
    public static final Color BACKGROUND = new Color(242, 242, 248);
    public static final int FIELD_HEIGHT = 40;
    public static final int BUTTON_HEIGHT = 34;
    public static final int BUTTON_MIN_WIDTH = 92;
    public static final int BUTTON_PADDING_X = 12;
    public static final int SEARCH_WIDTH = 300;
    public static final int FILTER_WIDTH = 200;
    public static final int TABLE_CELL_PADDING_X = 18;

    // 📐 Standard Layout Spacing
    public static final int PANEL_MARGIN = 40;
    public static final int TOP_GAP = 20;
    public static final int BOTTOM_GAP = 20;
    public static final int TITLE_HEIGHT = 51;
    public static final int TITLE_WIDTH = 308;
    public static final int TITLE_TO_TOOLBAR_GAP = 20;
    public static final int TOOLBAR_TO_CONTENT_GAP = 15;
    public static final Color PLACEHOLDER = new Color(148, 150, 163);
    public static final Color ERROR = new Color(220, 38, 38);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color TEXT = new Color(31, 41, 55);
    public static final Color MUTED_TEXT = new Color(107, 114, 128);

    public static void applyGlobalDialogStyle() {
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", new Font("Ubuntu", Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", new Font("Ubuntu", Font.BOLD, 13));
        UIManager.put("OptionPane.border", BorderFactory.createCompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(18, 20, 16, 20)
        ));
        UIManager.put("OptionPane.messageAreaBorder", BorderFactory.createEmptyBorder(4, 0, 12, 0));
        UIManager.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder(8, 0, 0, 0));
        UIManager.put("OptionPane.minimumSize", new Dimension(360, 150));
        UIManager.put("OptionPane.sameSizeButtons", true);
        UIManager.put("Button.arc", 10);
        UIManager.put("Button.margin", new Insets(6, 14, 6, 14));

        UIManager.put("OptionPane.informationIcon", new DialogStatusIcon(SUCCESS, "check"));
        UIManager.put("OptionPane.errorIcon", new DialogStatusIcon(ERROR, "x"));
        UIManager.put("OptionPane.warningIcon", new DialogStatusIcon(WARNING, "!"));
        UIManager.put("OptionPane.questionIcon", new DialogStatusIcon(PRIMARY, "?"));
    }

    private static class DialogStatusIcon implements Icon {
        private final Color color;
        private final String symbol;

        DialogStatusIcon(Color color, String symbol) {
            this.color = color;
            this.symbol = symbol;
        }

        @Override
        public int getIconWidth() {
            return 38;
        }

        @Override
        public int getIconHeight() {
            return 38;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
            g2.fillOval(x, y, 38, 38);
            g2.setColor(color);
            g2.fillOval(x + 6, y + 6, 26, 26);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + 19;
            int cy = y + 19;

            switch (symbol) {
                case "check" -> {
                    g2.drawLine(x + 14, y + 20, x + 18, y + 24);
                    g2.drawLine(x + 18, y + 24, x + 25, y + 15);
                }
                case "x" -> {
                    g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                    g2.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
                }
                case "!" -> {
                    g2.drawLine(cx, cy - 8, cx, cy + 2);
                    g2.fillOval(cx - 1, cy + 7, 3, 3);
                }
                default -> {
                    g2.setFont(new Font("Ubuntu", Font.BOLD, 18));
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = cx - fm.stringWidth("?") / 2;
                    int textY = cy + fm.getAscent() / 2 - 2;
                    g2.drawString("?", textX, textY);
                }
            }

            g2.dispose();
        }
    }

    public static void showSuccessMessage(Component parent, String message) {
        showAppMessage(parent, "Success", message, SUCCESS, "check");
    }

    public static void showErrorMessage(Component parent, String message) {
        showAppMessage(parent, "Something went wrong", message, ERROR, "x");
    }

    public static void showWarningMessage(Component parent, String message) {
        showAppMessage(parent, "Needs attention", message, WARNING, "!");
    }

    public static void showInfoMessage(Component parent, String message) {
        showAppMessage(parent, "Message", message, PRIMARY, "?");
    }

    public static void showMessageDialog(Component parent, Object message) {
        showInfoMessage(parent, String.valueOf(message));
    }

    public static void showMessageDialog(Component parent, Object message, String title, int messageType) {
        Color accent = switch (messageType) {
            case JOptionPane.ERROR_MESSAGE -> ERROR;
            case JOptionPane.WARNING_MESSAGE -> WARNING;
            case JOptionPane.INFORMATION_MESSAGE -> SUCCESS;
            case JOptionPane.QUESTION_MESSAGE -> PRIMARY;
            default -> PRIMARY;
        };
        String symbol = switch (messageType) {
            case JOptionPane.ERROR_MESSAGE -> "x";
            case JOptionPane.WARNING_MESSAGE -> "!";
            case JOptionPane.INFORMATION_MESSAGE -> "check";
            default -> "?";
        };
        showAppMessage(parent, normalizeDialogTitle(title, messageType), String.valueOf(message), accent, symbol);
    }

    public static boolean showConfirmMessage(Component parent, String message) {
        return showAppConfirm(parent, "Please confirm", message, WARNING, "!");
    }

    public static boolean showConfirmMessage(Component parent, String title, String message) {
        return showAppConfirm(parent, title, message, WARNING, "!");
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) {
        return showConfirmDialog(parent, message, title, optionType, JOptionPane.QUESTION_MESSAGE);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType) {
        Color accent = messageType == JOptionPane.WARNING_MESSAGE ? WARNING : PRIMARY;
        String symbol = messageType == JOptionPane.WARNING_MESSAGE ? "!" : "?";
        boolean confirmed = showAppConfirm(parent, normalizeDialogTitle(title, messageType), String.valueOf(message), accent, symbol);
        return confirmed ? JOptionPane.YES_OPTION : JOptionPane.NO_OPTION;
    }

    private static String normalizeDialogTitle(String title, int messageType) {
        if (title != null && !title.isBlank()) {
            return title;
        }
        return switch (messageType) {
            case JOptionPane.ERROR_MESSAGE -> "Something went wrong";
            case JOptionPane.WARNING_MESSAGE -> "Needs attention";
            case JOptionPane.INFORMATION_MESSAGE -> "Success";
            case JOptionPane.QUESTION_MESSAGE -> "Please confirm";
            default -> "Message";
        };
    }

    private static void showAppMessage(Component parent, String title, String message, Color accent, String symbol) {
        JDialog dialog = createAppDialog(parent);
        JPanel content = createDialogSurface();
        content.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 0, 18);
        content.add(new JLabel(new DialogStatusIcon(accent, symbol)), gbc);

        JPanel copyPanel = new JPanel(new BorderLayout(0, 8));
        copyPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Ubuntu", Font.BOLD, 21));
        titleLabel.setForeground(TEXT);

        JTextArea messageText = createDialogMessage(message);
        copyPanel.add(titleLabel, BorderLayout.NORTH);
        copyPanel.add(messageText, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(copyPanel, gbc);

        JButton okButton = createDialogButton("OK", true);
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets = new Insets(28, 0, 0, 0);
        content.add(buttonPanel, gbc);

        installDialogCloseActions(dialog, okButton);
        showDialog(dialog, content);
    }

    private static boolean showAppConfirm(Component parent, String title, String message, Color accent, String symbol) {
        final boolean[] confirmed = {false};
        JDialog dialog = createAppDialog(parent);
        JPanel content = createDialogSurface();
        content.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 0, 18);
        content.add(new JLabel(new DialogStatusIcon(accent, symbol)), gbc);

        JPanel copyPanel = new JPanel(new BorderLayout(0, 8));
        copyPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Ubuntu", Font.BOLD, 21));
        titleLabel.setForeground(TEXT);

        JTextArea messageText = createDialogMessage(message);
        copyPanel.add(titleLabel, BorderLayout.NORTH);
        copyPanel.add(messageText, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(copyPanel, gbc);

        JButton cancelButton = createDialogButton("Cancel", false);
        JButton confirmButton = createDialogButton("Confirm", true);

        cancelButton.addActionListener(e -> dialog.dispose());
        confirmButton.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets = new Insets(28, 0, 0, 0);
        content.add(buttonPanel, gbc);

        installDialogCloseActions(dialog, cancelButton);
        dialog.getRootPane().setDefaultButton(confirmButton);
        showDialog(dialog, content);
        return confirmed[0];
    }

    private static JDialog createAppDialog(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setResizable(false);
        return dialog;
    }

    private static JPanel createDialogSurface() {
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(new Color(229, 231, 235));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(28, 30, 32, 34));
        return content;
    }

    private static JTextArea createDialogMessage(String message) {
        JTextArea messageText = new JTextArea(message);
        messageText.setOpaque(false);
        messageText.setEditable(false);
        messageText.setFocusable(false);
        messageText.setLineWrap(true);
        messageText.setWrapStyleWord(true);
        messageText.setFont(new Font("Ubuntu", Font.PLAIN, 17));
        messageText.setForeground(TEXT);
        messageText.setBorder(BorderFactory.createEmptyBorder());
        messageText.setColumns(24);
        return messageText;
    }

    private static JButton createDialogButton(String text, boolean primary) {
        JButton button = new JButton(text);
        if (primary) {
            applyButton(button);
        } else {
            applySecondaryButton(button);
        }
        button.setFont(new Font("Ubuntu", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(primary ? 108 : 104, 38));
        return button;
    }

    private static void installDialogCloseActions(JDialog dialog, JButton fallbackButton) {
        JRootPane rootPane = dialog.getRootPane();
        rootPane.setDefaultButton(fallbackButton);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "closeDialog");
        rootPane.getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });
    }

    private static void showDialog(JDialog dialog, JPanel content) {
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, 190));
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
    }

    public static void applyModulePanel(JPanel panel) {
        panel.setBackground(BACKGROUND);
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createEmptyBorder(PANEL_MARGIN / 2, PANEL_MARGIN, BOTTOM_GAP, PANEL_MARGIN));
    }

    public static void applyToolbarPanel(JPanel panel) {
        panel.setBackground(BACKGROUND);
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, TOOLBAR_TO_CONTENT_GAP, 0));
    }

    public static void applyFormPanel(JPanel panel) {
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    // 🎯 Button Style
    public static void applyButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Ubuntu", Font.BOLD, 14));
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        int width = Math.max(BUTTON_MIN_WIDTH, btn.getPreferredSize().width + (BUTTON_PADDING_X * 2));
        btn.setPreferredSize(new Dimension(width, BUTTON_HEIGHT));
        btn.setBorder(BorderFactory.createEmptyBorder(0, BUTTON_PADDING_X, 0, BUTTON_PADDING_X));
        
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                JButton b = (JButton) c;
                int paintHeight = Math.min(BUTTON_HEIGHT, b.getHeight());
                int y = (b.getHeight() - paintHeight) / 2;
                if (b.getModel().isPressed()) {
                    g2.setColor(PRIMARY.darker());
                } else if (b.getModel().isRollover()) {
                    g2.setColor(new Color(40, 80, 180));
                } else {
                    g2.setColor(b.getBackground());
                }
                g2.fillRoundRect(0, y, b.getWidth(), paintHeight, 10, 10);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    // 🎯 Secondary Outlined Button Style
    public static void applySecondaryButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Ubuntu", Font.BOLD, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        int width = Math.max(BUTTON_MIN_WIDTH, btn.getPreferredSize().width + (BUTTON_PADDING_X * 2));
        btn.setPreferredSize(new Dimension(width, BUTTON_HEIGHT));
        btn.setBorder(BorderFactory.createEmptyBorder(0, BUTTON_PADDING_X, 0, BUTTON_PADDING_X));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                JButton b = (JButton) c;
                int paintHeight = Math.min(BUTTON_HEIGHT, b.getHeight());
                int y = (b.getHeight() - paintHeight) / 2;
                if (b.getModel().isPressed()) {
                    g2.setColor(new Color(230, 230, 230));
                } else if (b.getModel().isRollover()) {
                    g2.setColor(new Color(245, 245, 250));
                } else {
                    g2.setColor(b.getBackground());
                }
                g2.fillRoundRect(0, y, b.getWidth(), paintHeight, 10, 10);
                g2.setColor(PRIMARY);
                g2.drawRoundRect(0, y, b.getWidth() - 1, paintHeight - 1, 10, 10);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    // 🎯 Back Button Style (text-only, no border)
    public static void applyBackButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Ubuntu", Font.BOLD, 14));
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

    public static void applyTextArea(JTextArea area) {
        area.setFont(new Font("Ubuntu", Font.PLAIN, 14));
        area.setBackground(Color.WHITE);
        area.setForeground(new Color(40, 40, 40));
        area.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 210), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        rememberNormalBorder(area);
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
        rememberNormalBorder(field);
    }

    public static void applyPasswordToggle(JPasswordField field) {
        field.putClientProperty("normalEchoChar", field.getEchoChar());
        field.putClientProperty("passwordVisible", false);

        javax.swing.border.Border passwordBorder = createPasswordBorder(new Color(200, 200, 210));

        field.setBorder(passwordBorder);
        field.putClientProperty("normalBorder", passwordBorder);

        if (Boolean.TRUE.equals(field.getClientProperty("passwordToggleInstalled"))) {
            return;
        }
        field.putClientProperty("passwordToggleInstalled", true);
        field.setToolTipText("Show password");

        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getX() < field.getWidth() - 42 || isPlaceholder(field)) {
                    return;
                }

                boolean visible = Boolean.TRUE.equals(field.getClientProperty("passwordVisible"));
                Object echo = field.getClientProperty("normalEchoChar");
                field.putClientProperty("passwordVisible", !visible);
                field.setEchoChar(visible && echo instanceof Character character ? character : (char) 0);
                field.setToolTipText(visible ? "Show password" : "Hide password");
                field.repaint();
            }
        });

        field.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                field.setCursor(new Cursor(e.getX() >= field.getWidth() - 42 ? Cursor.HAND_CURSOR : Cursor.TEXT_CURSOR));
            }
        });
    }

    private static javax.swing.border.Border createPasswordBorder(Color lineColor) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(lineColor, 1, true),
                new javax.swing.border.AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        boolean visible = Boolean.TRUE.equals(((JComponent) c).getClientProperty("passwordVisible"));
                        int iconSize = 18;
                        int iconX = x + width - 30;
                        int iconY = y + (height - iconSize) / 2;
                        int centerY = iconY + iconSize / 2;

                        g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.setColor(new Color(90, 96, 110));
                        g2.drawArc(iconX, iconY + 3, iconSize, iconSize - 6, 20, 140);
                        g2.drawArc(iconX, iconY + 3, iconSize, iconSize - 6, 200, 140);
                        g2.drawOval(iconX + 6, centerY - 3, 6, 6);

                        if (visible) {
                            g2.drawLine(iconX + 1, iconY + iconSize - 2, iconX + iconSize - 1, iconY + 2);
                        }

                        g2.dispose();
                    }

                    @Override
                    public Insets getBorderInsets(Component c) {
                        return new Insets(8, 14, 8, 42);
                    }
                }
        );
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
        rememberNormalBorder(field);
    }

    public static void applyPlaceholder(JTextComponent field, String placeholder) {
        if (field == null || placeholder == null || placeholder.isBlank()) {
            return;
        }

        rememberNormalBorder(field);
        field.putClientProperty("placeholderText", placeholder);
        if (field instanceof JPasswordField passwordField) {
            passwordField.putClientProperty("normalEchoChar", passwordField.getEchoChar());
        }

        if (field.getText().trim().isEmpty()) {
            showPlaceholder(field);
        }

        if (Boolean.TRUE.equals(field.getClientProperty("placeholderInstalled"))) {
            return;
        }
        field.putClientProperty("placeholderInstalled", true);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                clearFieldError(field);
                if (isPlaceholder(field)) {
                    field.setText("");
                    field.setForeground(Color.DARK_GRAY);
                    field.putClientProperty("placeholderVisible", false);
                    if (field instanceof JPasswordField passwordField) {
                        Object echo = passwordField.getClientProperty("normalEchoChar");
                        if (echo instanceof Character character) {
                            passwordField.setEchoChar(character);
                        }
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    showPlaceholder(field);
                }
            }
        });
    }

    public static boolean isPlaceholder(JTextComponent field) {
        Object placeholder = field.getClientProperty("placeholderText");
        return Boolean.TRUE.equals(field.getClientProperty("placeholderVisible"))
                && placeholder != null
                && field.getText().equals(placeholder.toString());
    }

    public static String getFieldText(JTextComponent field) {
        return isPlaceholder(field) ? "" : field.getText().trim();
    }

    public static void showFieldError(JComponent field) {
        rememberNormalBorder(field);
        if (field instanceof JPasswordField && Boolean.TRUE.equals(field.getClientProperty("passwordToggleInstalled"))) {
            field.setBorder(createPasswordBorder(ERROR));
            field.requestFocusInWindow();
            return;
        }
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ERROR, 1, true),
                BorderFactory.createEmptyBorder(0, 14, 0, 14)
        ));
        field.requestFocusInWindow();
    }

    public static void showTextAreaError(JTextComponent field) {
        rememberNormalBorder(field);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ERROR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.requestFocusInWindow();
    }

    public static void clearFieldError(JComponent field) {
        Object border = field.getClientProperty("normalBorder");
        if (border instanceof javax.swing.border.Border normalBorder) {
            field.setBorder(normalBorder);
        }
    }

    private static void rememberNormalBorder(JComponent field) {
        if (field.getClientProperty("normalBorder") == null) {
            field.putClientProperty("normalBorder", field.getBorder());
        }
    }

    private static void showPlaceholder(JTextComponent field) {
        Object placeholder = field.getClientProperty("placeholderText");
        if (placeholder == null) {
            return;
        }
        if (field instanceof JPasswordField passwordField) {
            passwordField.setEchoChar((char) 0);
        }
        field.setText(placeholder.toString());
        field.setForeground(PLACEHOLDER);
        field.putClientProperty("placeholderVisible", true);
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
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
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

    public static void applyTableContainer(JScrollPane scrollPane) {
        applyScrollStyle(scrollPane);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(Color.WHITE);
    }

    public static void applyTableActionTooltips(JTable table, java.util.Map<Integer, String> tooltipsByModelColumn) {
        table.putClientProperty("actionTooltips", tooltipsByModelColumn);
        if (Boolean.TRUE.equals(table.getClientProperty("actionTooltipsInstalled"))) {
            return;
        }

        table.setToolTipText("");
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                JTable source = (JTable) e.getSource();
                int viewColumn = source.columnAtPoint(e.getPoint());
                if (viewColumn < 0) {
                    source.setToolTipText(null);
                    return;
                }

                int modelColumn = source.convertColumnIndexToModel(viewColumn);
                Object value = source.getClientProperty("actionTooltips");
                if (value instanceof java.util.Map<?, ?> map) {
                    Object tooltip = map.get(modelColumn);
                    source.setToolTipText(tooltip instanceof String text ? text : null);
                }
            }
        });
        table.putClientProperty("actionTooltipsInstalled", true);
    }

    // 📅 JDateChooser Style
    public static void applyDateChooserStyle(com.toedter.calendar.JDateChooser chooser) {
        chooser.setFont(new Font("Ubuntu", Font.PLAIN, 15));
        chooser.setBackground(Color.WHITE);
        chooser.setOpaque(false);
        chooser.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 210), 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        chooser.setPreferredSize(new Dimension(chooser.getPreferredSize().width, FIELD_HEIGHT));
        rememberNormalBorder(chooser);

        // Style the text field inside
        JTextField editor = (JTextField) chooser.getDateEditor().getUiComponent();
        editor.setFont(new Font("Ubuntu", Font.PLAIN, 15));
        editor.setBackground(Color.WHITE);
        editor.setForeground(Color.DARK_GRAY);
        editor.setPreferredSize(new Dimension(editor.getPreferredSize().width, FIELD_HEIGHT));
        editor.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
        editor.setOpaque(true);

        // Style the button
        JButton button = chooser.getCalendarButton();
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Open calendar");
        button.setText("");
        button.setIcon(null);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(42, FIELD_HEIGHT));
        button.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 12));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                JButton b = (JButton) c;
                if (b.getModel().isRollover()) {
                    g2.setColor(new Color(245, 247, 252));
                    g2.fillRoundRect(4, 5, b.getWidth() - 8, b.getHeight() - 10, 8, 8);
                }

                int x = (b.getWidth() - 18) / 2;
                int y = (b.getHeight() - 18) / 2;
                g2.setColor(PRIMARY);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 1, y + 3, 16, 14, 4, 4);
                g2.drawLine(x + 1, y + 7, x + 17, y + 7);
                g2.drawLine(x + 5, y + 1, x + 5, y + 4);
                g2.drawLine(x + 13, y + 1, x + 13, y + 4);

                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 5, y + 11, x + 5, y + 11);
                g2.drawLine(x + 9, y + 11, x + 9, y + 11);
                g2.drawLine(x + 13, y + 11, x + 13, y + 11);
                g2.drawLine(x + 5, y + 14, x + 5, y + 14);
                g2.drawLine(x + 9, y + 14, x + 9, y + 14);

                g2.dispose();
                super.paint(g, c);
            }
        });
    }

public static void applyTableStyle(JTable table, int fontSize, int headerFontSize) {

    // 🧠 Basic table settings
    table.setRowHeight(fontSize + 24);
    table.setFont(new Font("Ubuntu", Font.PLAIN, fontSize));
    table.setForeground(new Color(40, 40, 40));
    table.setBackground(Color.WHITE);
    table.setOpaque(true);
    table.setGridColor(new Color(235, 237, 242));

    table.setShowHorizontalLines(true);
    table.setShowVerticalLines(false);
    table.setIntercellSpacing(new Dimension(0, 0));
    table.setSelectionBackground(new Color(220, 235, 255));
    table.setSelectionForeground(Color.BLACK);
    table.setFillsViewportHeight(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    table.setRowMargin(0);
    table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            applyTableCellPadding(label);
            return label;
        }
    });

    // 🎯 Header styling
    JTableHeader header = table.getTableHeader();

    header.setFont(new Font("Ubuntu", Font.BOLD, headerFontSize));
    header.setBackground(new Color(242, 242, 248));
    header.setForeground(new Color(60, 60, 60));
    header.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    header.setReorderingAllowed(false);

    // ? FORCE ALL CAPS RENDERING
    header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel label = new JLabel(String.valueOf(value).toUpperCase());
            label.setFont(new Font("Ubuntu", Font.BOLD, headerFontSize));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setBorder(BorderFactory.createEmptyBorder(12, TABLE_CELL_PADDING_X, 12, TABLE_CELL_PADDING_X));
            label.setOpaque(true);
            label.setBackground(new Color(242, 242, 248));
            label.setForeground(new Color(60, 60, 60));

            return label;
        }
    });
}

    public static void applyTableCellPadding(JLabel label) {
        label.setBorder(BorderFactory.createEmptyBorder(0, TABLE_CELL_PADDING_X, 0, TABLE_CELL_PADDING_X));
    }

    public static Icon createSvgIcon(String resourcePath, int size) {
        return new SvgResourceIcon(resourcePath, size);
    }

    public static JPanel createFrameTitleBar(JFrame frame, String title, boolean allowMaximize) {
        JPanel titleBar = new JPanel(new BorderLayout(16, 0));
        titleBar.setBackground(Color.WHITE);
        titleBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(8, 16, 8, 10)
        ));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);

        JButton minimizeButton = createFrameControlButton("/images/minus.svg");
        minimizeButton.setToolTipText("Minimize");
        minimizeButton.addActionListener(e -> frame.setState(Frame.ICONIFIED));
        controls.add(minimizeButton);

        if (allowMaximize) {
            JButton maximizeButton = createFrameControlButton("/images/fullscreen.svg");
            maximizeButton.setToolTipText("Maximize");
            maximizeButton.addActionListener(e -> {
                int state = frame.getExtendedState();
                boolean maximized = (state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
                frame.setExtendedState(maximized ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
            });
            controls.add(maximizeButton);
        }

        JButton closeButton = createFrameControlButton("/images/x.svg");
        closeButton.setToolTipText("Close");
        closeButton.addActionListener(e -> frame.dispatchEvent(new java.awt.event.WindowEvent(frame, java.awt.event.WindowEvent.WINDOW_CLOSING)));
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setBackground(ERROR);
                closeButton.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setBackground(Color.WHITE);
                closeButton.setForeground(TEXT);
            }
        });
        controls.add(closeButton);

        titleBar.add(controls, BorderLayout.EAST);

        final Point[] dragPoint = {null};
        MouseAdapter dragHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragPoint[0] = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] == null || (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                    return;
                }
                Point location = e.getLocationOnScreen();
                frame.setLocation(location.x - dragPoint[0].x, location.y - dragPoint[0].y);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (allowMaximize && e.getClickCount() == 2) {
                    int state = frame.getExtendedState();
                    boolean maximized = (state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
                    frame.setExtendedState(maximized ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
                }
            }
        };
        titleBar.addMouseListener(dragHandler);
        titleBar.addMouseMotionListener(dragHandler);

        return titleBar;
    }

    private static JButton createFrameControlButton(String iconPath) {
        JButton button = new JButton(createSvgIcon(iconPath, 16));
        button.setPreferredSize(new Dimension(34, 28));
        button.setMinimumSize(new Dimension(34, 28));
        button.setFont(new Font("Ubuntu", Font.BOLD, 14));
        button.setForeground(TEXT);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(245, 247, 252));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    private static class SvgResourceIcon implements Icon {
        private static final Pattern PATH_PATTERN = Pattern.compile("<path\\b[^>]*\\sd=\"([^\"]+)\"[^>]*/?>");
        private static final Pattern RECT_PATTERN = Pattern.compile("<rect\\b([^>]*)/?>");
        private static final Pattern ATTR_PATTERN = Pattern.compile("(\\w+)=\"([^\"]+)\"");
        private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z]|[-+]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE][-+]?\\d+)?");

        private final int size;
        private final List<Shape> shapes = new ArrayList<>();

        SvgResourceIcon(String resourcePath, int size) {
            this.size = size;
            load(resourcePath);
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.scale(size / 24.0, size / 24.0);
            g2.setColor(c == null ? TEXT : c.getForeground());
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Shape shape : shapes) {
                g2.draw(shape);
            }
            g2.dispose();
        }

        private void load(String resourcePath) {
            try (InputStream in = style.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    return;
                }

                StringBuilder svg = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        svg.append(line);
                    }
                }

                Matcher pathMatcher = PATH_PATTERN.matcher(svg);
                while (pathMatcher.find()) {
                    shapes.add(parsePath(pathMatcher.group(1)));
                }

                Matcher rectMatcher = RECT_PATTERN.matcher(svg);
                while (rectMatcher.find()) {
                    shapes.add(parseRect(rectMatcher.group(1)));
                }
            } catch (Exception ignored) {
                shapes.clear();
            }
        }

        private Shape parseRect(String attrs) {
            double x = attr(attrs, "x", 0);
            double y = attr(attrs, "y", 0);
            double width = attr(attrs, "width", 0);
            double height = attr(attrs, "height", 0);
            double radius = attr(attrs, "rx", 0);
            return new RoundRectangle2D.Double(x, y, width, height, radius * 2, radius * 2);
        }

        private double attr(String attrs, String name, double fallback) {
            Matcher matcher = ATTR_PATTERN.matcher(attrs);
            while (matcher.find()) {
                if (name.equals(matcher.group(1))) {
                    return Double.parseDouble(matcher.group(2));
                }
            }
            return fallback;
        }

        private Shape parsePath(String data) {
            List<String> tokens = new ArrayList<>();
            Matcher matcher = TOKEN_PATTERN.matcher(data.replace(',', ' '));
            while (matcher.find()) {
                tokens.add(matcher.group());
            }

            Path2D.Double path = new Path2D.Double();
            double x = 0;
            double y = 0;
            double startX = 0;
            double startY = 0;
            char command = 'M';

            for (int i = 0; i < tokens.size();) {
                String token = tokens.get(i);
                if (isCommand(token)) {
                    command = token.charAt(0);
                    i++;
                    if (command == 'Z' || command == 'z') {
                        path.closePath();
                        x = startX;
                        y = startY;
                    }
                    continue;
                }

                char op = command;
                switch (op) {
                    case 'M', 'm' -> {
                        double nx = next(tokens, i++);
                        double ny = next(tokens, i++);
                        x = op == 'm' ? x + nx : nx;
                        y = op == 'm' ? y + ny : ny;
                        path.moveTo(x, y);
                        startX = x;
                        startY = y;
                        command = op == 'm' ? 'l' : 'L';
                    }
                    case 'L', 'l' -> {
                        double nx = next(tokens, i++);
                        double ny = next(tokens, i++);
                        x = op == 'l' ? x + nx : nx;
                        y = op == 'l' ? y + ny : ny;
                        path.lineTo(x, y);
                    }
                    case 'H', 'h' -> {
                        double nx = next(tokens, i++);
                        x = op == 'h' ? x + nx : nx;
                        path.lineTo(x, y);
                    }
                    case 'V', 'v' -> {
                        double ny = next(tokens, i++);
                        y = op == 'v' ? y + ny : ny;
                        path.lineTo(x, y);
                    }
                    case 'A', 'a' -> {
                        i += 5;
                        double nx = next(tokens, i++);
                        double ny = next(tokens, i++);
                        x = op == 'a' ? x + nx : nx;
                        y = op == 'a' ? y + ny : ny;
                        path.lineTo(x, y);
                    }
                    default -> i++;
                }
            }
            return path;
        }

        private boolean isCommand(String token) {
            return token.length() == 1 && Character.isLetter(token.charAt(0));
        }

        private double next(List<String> tokens, int index) {
            return Double.parseDouble(tokens.get(index).toLowerCase(Locale.ROOT));
        }
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
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(200, 200, 210));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        mainPanel.setOpaque(true);
        mainPanel.setBackground(Color.WHITE);

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
        
        // Ensure content is opaque for stable rendering and no blinking while typing
        content.setOpaque(true);
        content.setBackground(Color.WHITE);
        
        contentWrapper.add(content, BorderLayout.CENTER);
        mainPanel.add(contentWrapper, BorderLayout.CENTER);

        dialog.setContentPane(mainPanel);
    }
}
