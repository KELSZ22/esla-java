/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui;

import com.kelsz.esla.MainFrame;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 *
 * @author kelsz-dev
 */
public class HeaderNav extends javax.swing.JPanel {

    private MainFrame frame;
    private static final Color PRIMARY_COLOR = style.PRIMARY;
    private static final Color HOVER_COLOR = new Color(245, 245, 250);
    private static final Color TEXT_COLOR = new Color(100, 100, 110);
    private static final Color ACTIVE_TEXT_COLOR = style.PRIMARY;
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    
    private JButton reloadButton;
    private JButton exportButton;
    private JButton userDropdown;
    private JPopupMenu exportMenu;
    private JPopupMenu logoutMenu;

    /**
     * Creates new form HeaderNav
     */
    public HeaderNav(MainFrame frame) {
        this.frame = frame;

        initComponents();
        setupUserArea();
        setupModernStyle();
        initEvents();
        setActive(dashboardNav);
    }

    private void setupModernStyle() {
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        setupNavButton(dashboardNav, "Dashboard");
        setupNavButton(ledgerNav, "Ledger");
        setupNavButton(memberNav, "Members");
        setupNavButton(serviceNav, "Service");
        setupNavButton(dividendNav, "Dividend");

        jLabel1.setFont(new Font("Ubuntu", Font.BOLD, 24));
        jLabel1.setForeground(PRIMARY_COLOR);
    }

    private void setupUserArea() {
        // Reload Button
        reloadButton = new JButton();
        reloadButton.setToolTipText("Reload Current Page");
        reloadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reloadButton.setBorderPainted(false);
        reloadButton.setFocusPainted(false);
        reloadButton.setContentAreaFilled(false);
        reloadButton.setOpaque(true);
        reloadButton.setBackground(BACKGROUND_COLOR);
        reloadButton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        java.net.URL reloadIconUrl = getClass().getResource("/images/rotate-ccw.png");
        if (reloadIconUrl != null) {
            reloadButton.setIcon(new ImageIcon(new ImageIcon(reloadIconUrl).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        }
        
        reloadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { reloadButton.setBackground(HOVER_COLOR); }
            @Override
            public void mouseExited(MouseEvent e) { reloadButton.setBackground(BACKGROUND_COLOR); }
        });
        
        reloadButton.addActionListener(e -> frame.refreshActivePage());

        // Export Dropdown
        exportButton = new JButton(style.createSvgIcon("/images/file-down.svg", 20));
        exportButton.setToolTipText("Export");
        exportButton.setPreferredSize(new Dimension(36, 36));
        exportButton.setForeground(TEXT_COLOR);
        exportButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportButton.setBorderPainted(false);
        exportButton.setFocusPainted(false);
        exportButton.setContentAreaFilled(false);
        exportButton.setOpaque(true);
        exportButton.setBackground(BACKGROUND_COLOR);
        exportButton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { exportButton.setBackground(HOVER_COLOR); }
            @Override
            public void mouseExited(MouseEvent e) { exportButton.setBackground(BACKGROUND_COLOR); }
        });

        exportMenu = new JPopupMenu();
        JMenuItem exportPdfItem = new JMenuItem("Export PDF");
        JMenuItem exportExcelItem = new JMenuItem("Export Excel");
        exportPdfItem.setFont(new Font("Ubuntu", Font.PLAIN, 14));
        exportExcelItem.setFont(new Font("Ubuntu", Font.PLAIN, 14));
        exportPdfItem.addActionListener(e -> frame.exportActivePage("pdf"));
        exportExcelItem.addActionListener(e -> frame.exportActivePage("excel"));
        exportMenu.add(exportPdfItem);
        exportMenu.add(exportExcelItem);

        exportButton.addActionListener(e -> exportMenu.show(exportButton, 0, exportButton.getHeight()));

        // User Dropdown
        String userName = com.kelsz.esla.UserSession.getInstance().getName();
        userDropdown = new JButton(userName);
        userDropdown.setFont(new Font("Ubuntu", Font.BOLD, 14));
        userDropdown.setForeground(TEXT_COLOR);
        userDropdown.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userDropdown.setBorderPainted(false);
        userDropdown.setFocusPainted(false);
        userDropdown.setContentAreaFilled(false);
        userDropdown.setOpaque(true);
        userDropdown.setBackground(BACKGROUND_COLOR);
        userDropdown.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        java.net.URL userIconUrl = getClass().getResource("/images/circle-user-round.png");
        if (userIconUrl != null) {
            userDropdown.setIcon(new ImageIcon(new ImageIcon(userIconUrl).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
            userDropdown.setIconTextGap(10);
        }
        
        userDropdown.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { userDropdown.setBackground(HOVER_COLOR); }
            @Override
            public void mouseExited(MouseEvent e) { userDropdown.setBackground(BACKGROUND_COLOR); }
        });

        // Logout Menu
        logoutMenu = new JPopupMenu();
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setFont(new Font("Ubuntu", Font.PLAIN, 14));
        java.net.URL logoutIconUrl = getClass().getResource("/images/log-out.png");
        if (logoutIconUrl != null) {
            logoutItem.setIcon(new ImageIcon(new ImageIcon(logoutIconUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        }
        logoutItem.addActionListener(e -> {
            com.kelsz.esla.UserSession.getInstance().logout();
            frame.dispose();
            new com.kelsz.esla.features.auth.Login().setVisible(true);
        });
        logoutMenu.add(logoutItem);
        
        userDropdown.addActionListener(e -> logoutMenu.show(userDropdown, 0, userDropdown.getHeight()));

        rebuildHeaderLayout();
    }

    private void rebuildHeaderLayout() {
        removeAll();
        setLayout(new BorderLayout(24, 0));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        navPanel.setOpaque(false);
        navPanel.add(dashboardNav);
        navPanel.add(ledgerNav);
        navPanel.add(memberNav);
        navPanel.add(serviceNav);
        navPanel.add(dividendNav);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(exportButton);
        actionPanel.add(reloadButton);
        actionPanel.add(userDropdown);

        add(navPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.EAST);
        revalidate();
        repaint();
    }

    private void setupNavButton(JButton button, String text) {
        button.setText(text);
        button.setFont(new Font("Ubuntu", Font.PLAIN, 16));
        button.setForeground(TEXT_COLOR);
        button.setBackground(BACKGROUND_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.getFont().isBold()) {
                    button.setBackground(HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.getFont().isBold()) {
                    button.setBackground(BACKGROUND_COLOR);
                }
            }
        });
    }

    private void initEvents() {
        dashboardNav.addActionListener(e -> {
            frame.showPage("dashboard");
            setActive(dashboardNav);
        });

        ledgerNav.addActionListener(e -> {
            frame.showPage("ledger");
            setActive(ledgerNav);
        });

        memberNav.addActionListener(e -> {
            frame.showPage("member");
            setActive(memberNav);
        });

        serviceNav.addActionListener(e -> {
            frame.showPage("service");
            setActive(serviceNav);
        });

        dividendNav.addActionListener(e -> {
            frame.showPage("dividend");
            setActive(dividendNav);
        });
    }

    private void setActive(JButton active) {
        JButton[] all = {dashboardNav, ledgerNav, memberNav, serviceNav, dividendNav};

        for (JButton btn : all) {
            btn.setBackground(BACKGROUND_COLOR);
            btn.setForeground(TEXT_COLOR);
            btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
            btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        }

        active.setForeground(ACTIVE_TEXT_COLOR);
        active.setFont(active.getFont().deriveFont(Font.BOLD));
        active.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(12, 20, 12, 20),
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR)
        ));
        revalidate();
        repaint();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dashboardNav = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        ledgerNav = new javax.swing.JButton();
        memberNav = new javax.swing.JButton();
        serviceNav = new javax.swing.JButton();
        dividendNav = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        dashboardNav.setText("Dashboard");
        dashboardNav.addActionListener(this::dashboardNavActionPerformed);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/esla-logo1.png"))); // NOI18N

        ledgerNav.setText("Ledger");

        memberNav.setText("Member");

        serviceNav.setText("Service Charge");
        serviceNav.addActionListener(this::serviceNavActionPerformed);

        dividendNav.setText("Dividend");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addGap(40, 40, 40)
                .addComponent(dashboardNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(ledgerNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(memberNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(serviceNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(dividendNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(dashboardNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ledgerNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memberNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serviceNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dividendNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void dashboardNavActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardNavActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dashboardNavActionPerformed

    private void serviceNavActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serviceNavActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_serviceNavActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton dashboardNav;
    private javax.swing.JButton dividendNav;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton ledgerNav;
    private javax.swing.JButton memberNav;
    private javax.swing.JButton serviceNav;
    // End of variables declaration//GEN-END:variables
}
