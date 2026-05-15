package features;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import services.DividendService;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import com.kelsz.esla.util.ReportExporter;
import java.time.LocalDate;
import ui.style;

/**
 *
 * @author kelsz-dev
 */
public class dividend extends javax.swing.JPanel implements ui.Refreshable {
    
    @Override
    public void refresh() {
        loadServiceChargeDescriptions();
        loadDividendData();
    }

    private final DividendService dividendService;
    private final Timer searchTimer;
    private javax.swing.JButton exportPdfButton;
    private javax.swing.JButton exportExcelButton;
    private final List<String> serviceChargeDates = new java.util.ArrayList<>();
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat percentageFormat = new DecimalFormat("0.000000000");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Creates new form dividend
     */
    public dividend() {
        dividendService = new DividendService();
        initComponents();
        setupStyles();

        // Setup debounced search
        searchTimer = new Timer(300, (ActionEvent e) -> {
            loadDividendData();
        });
        searchTimer.setRepeats(false);

        setupListeners();
        loadServiceChargeDescriptions();
        loadDividendData();
    }

    private void setupStyles() {
        style.applyModernLabel(jLabel1, true);
        style.applySearchField(dividendSearchField);
        style.applyComboBox(selectServiceCharge);
        style.applyModernLabel(dateLabel, false);
        
        style.applyCardPanel(cardTotalPremium);
        style.applyCardPanel(cardPremiumPct);
        style.applyCardPanel(cardTotalInterest);
        style.applyCardPanel(cardRefund60);
        style.applyCardPanel(cardRefund40);
        style.applyCardPanel(cardTotalDividend);

        style.applyModernLabel(lblTotalPremium, false);
        style.applyModernLabel(lblPremiumPct, false);
        style.applyModernLabel(lblTotalInterest, false);
        style.applyModernLabel(lblRefund60, false);
        style.applyModernLabel(lblRefund40, false);
        style.applyModernLabel(lblTotalDividend, false);

        style.applyModernLabel(valTotalPremium, true);
        style.applyModernLabel(valPremiumPct, true);
        style.applyModernLabel(valTotalInterest, true);
        style.applyModernLabel(valRefund60, true);
        style.applyModernLabel(valRefund40, true);
        style.applyModernLabel(valTotalDividend, true);

        style.applyTableStyle(dividendTable, 14, 14);
        style.applyScrollStyle(jScrollPane1);
        
        setupExportButtons();
        style.applyStandardSizes(dividendSearchField, selectServiceCharge);
    }

    private void setupExportButtons() {
        exportPdfButton = new javax.swing.JButton("Export PDF");
        exportExcelButton = new javax.swing.JButton("Export Excel");
        
        style.applyButton(exportPdfButton);
        style.applyButton(exportExcelButton);
        
        // Ensure buttons are visible by setting FlowLayout (GroupLayout can be restrictive for dynamic additions)
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        jPanel1.add(dividendSearchField);
        jPanel1.add(selectServiceCharge);
        jPanel1.add(dateLabel);
        jPanel1.add(exportPdfButton);
        jPanel1.add(exportExcelButton);
        
        exportPdfButton.addActionListener(e -> exportToPDF());
        exportExcelButton.addActionListener(e -> exportToExcel());
    }

    private void exportToExcel() {
        ReportExporter.exportToExcel(dividendTable, "Dividend Report", "Service Charge: " + selectServiceCharge.getSelectedItem(), new int[]{0, 1, 2, 3, 4, 5, 6, 7});
    }

    private void exportToPDF() {
        ReportExporter.exportToPDF(dividendTable, "Dividend Report", "Service Charge: " + selectServiceCharge.getSelectedItem(), new int[]{0, 1, 2, 3, 4, 5, 6, 7});
    }

    private void setupListeners() {
        dividendSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { searchTimer.restart(); }
        });

        selectServiceCharge.addActionListener(e -> {
            int idx = selectServiceCharge.getSelectedIndex();
            if (idx >= 0 && idx < serviceChargeDates.size()) {
                String date = serviceChargeDates.get(idx);
                dateLabel.setText("Date: " + (date != null ? date : "—"));
            }
            loadDividendData();
        });
    }

    private void loadServiceChargeDescriptions() {
        selectServiceCharge.removeAllItems();
        serviceChargeDates.clear();
        
        selectServiceCharge.addItem("All Service Charges");
        serviceChargeDates.add(null); // No specific date for "All"
        
        List<Map<String, Object>> details = dividendService.getServiceChargeDetails();
        for (Map<String, Object> detail : details) {
            selectServiceCharge.addItem((String) detail.get("description"));
            serviceChargeDates.add((String) detail.get("date_to"));
        }
        
        // Initial label update
        dateLabel.setText("Date: —");
    }

    private void loadDividendData() {
        String name = dividendSearchField.getText().trim();
        String serviceCharge = selectServiceCharge.getSelectedItem() != null ? selectServiceCharge.getSelectedItem().toString() : "All Service Charges";
        if (serviceCharge.equals("All Service Charges")) {
            serviceCharge = null;
        }
        
        String date = null;
        int idx = selectServiceCharge.getSelectedIndex();
        if (idx >= 0 && idx < serviceChargeDates.size()) {
            date = serviceChargeDates.get(idx);
        }

        Map<String, Object> data = dividendService.getDividendData(date, name, serviceCharge);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) data.get("rows");
        Map<String, Object> totals = (Map<String, Object>) data.get("totals");

        updateTable(rows);
        updateSummary(totals);
    }

    private void updateTable(List<Map<String, Object>> rows) {
        DefaultTableModel model = (DefaultTableModel) dividendTable.getModel();
        model.setRowCount(0);

        for (Map<String, Object> row : rows) {
            model.addRow(new Object[]{
                row.get("member_name"),
                formatCurrency((BigDecimal) row.get("premium_total")),
                formatPercentage((BigDecimal) row.get("premium_percentage")),
                formatCurrency((BigDecimal) row.get("total_interest")),
                formatCurrency((BigDecimal) row.get("refund_60")),
                formatCurrency((BigDecimal) row.get("refund_40")),
                formatCurrency((BigDecimal) row.get("dividend")),
                formatCurrency((BigDecimal) row.get("total_dividend"))
            });
        }
    }

    private void updateSummary(Map<String, Object> totals) {
        valTotalPremium.setText(formatCurrency((BigDecimal) totals.get("total_premium")));
        valPremiumPct.setText(formatPercentage((BigDecimal) totals.get("premium_percentage")));
        valTotalInterest.setText(formatCurrency((BigDecimal) totals.get("total_interest")));
        valRefund60.setText(formatCurrency((BigDecimal) totals.get("refund_60")));
        valRefund40.setText(formatCurrency((BigDecimal) totals.get("refund_40")));
        valTotalDividend.setText(formatCurrency((BigDecimal) totals.get("total_dividend")));
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0.00";
        return currencyFormat.format(amount);
    }

    private String formatPercentage(BigDecimal pct) {
        if (pct == null) return "0.000000000";
        return percentageFormat.format(pct);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        dividendSearchField = new javax.swing.JTextField();
        selectServiceCharge = new javax.swing.JComboBox<>();
        dateLabel = new javax.swing.JLabel();
        cardTotalPremium = new javax.swing.JPanel();
        lblTotalPremium = new javax.swing.JLabel();
        valTotalPremium = new javax.swing.JLabel();
        cardPremiumPct = new javax.swing.JPanel();
        lblPremiumPct = new javax.swing.JLabel();
        valPremiumPct = new javax.swing.JLabel();
        cardTotalInterest = new javax.swing.JPanel();
        lblTotalInterest = new javax.swing.JLabel();
        valTotalInterest = new javax.swing.JLabel();
        cardRefund60 = new javax.swing.JPanel();
        lblRefund60 = new javax.swing.JLabel();
        valRefund60 = new javax.swing.JLabel();
        cardRefund40 = new javax.swing.JPanel();
        lblRefund40 = new javax.swing.JLabel();
        valRefund40 = new javax.swing.JLabel();
        cardTotalDividend = new javax.swing.JPanel();
        lblTotalDividend = new javax.swing.JLabel();
        valTotalDividend = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        dividendTable = new javax.swing.JTable();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel1.setText("DIVIDEND");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        dateLabel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        dateLabel.setText("Form Date:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dividendSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectServiceCharge, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dividendSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectServiceCharge, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        cardTotalPremium.setBackground(new java.awt.Color(255, 255, 255));
        cardTotalPremium.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lblTotalPremium.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblTotalPremium.setText("Total Premium");

        valTotalPremium.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        valTotalPremium.setText("0.00");

        javax.swing.GroupLayout cardTotalPremiumLayout = new javax.swing.GroupLayout(cardTotalPremium);
        cardTotalPremium.setLayout(cardTotalPremiumLayout);
        cardTotalPremiumLayout.setHorizontalGroup(
            cardTotalPremiumLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTotalPremiumLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardTotalPremiumLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalPremium)
                    .addComponent(valTotalPremium))
                .addContainerGap(88, Short.MAX_VALUE))
        );
        cardTotalPremiumLayout.setVerticalGroup(
            cardTotalPremiumLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTotalPremiumLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTotalPremium)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valTotalPremium)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardPremiumPct.setBackground(new java.awt.Color(255, 255, 255));
        cardPremiumPct.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lblPremiumPct.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblPremiumPct.setText("Premium %");

        valPremiumPct.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        valPremiumPct.setText("0.000000000");

        javax.swing.GroupLayout cardPremiumPctLayout = new javax.swing.GroupLayout(cardPremiumPct);
        cardPremiumPct.setLayout(cardPremiumPctLayout);
        cardPremiumPctLayout.setHorizontalGroup(
            cardPremiumPctLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPremiumPctLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardPremiumPctLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPremiumPct)
                    .addComponent(valPremiumPct))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        cardPremiumPctLayout.setVerticalGroup(
            cardPremiumPctLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPremiumPctLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPremiumPct)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valPremiumPct)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardTotalInterest.setBackground(new java.awt.Color(255, 255, 255));
        cardTotalInterest.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lblTotalInterest.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblTotalInterest.setText("Total Interest");

        valTotalInterest.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        valTotalInterest.setText("0.00");

        javax.swing.GroupLayout cardTotalInterestLayout = new javax.swing.GroupLayout(cardTotalInterest);
        cardTotalInterest.setLayout(cardTotalInterestLayout);
        cardTotalInterestLayout.setHorizontalGroup(
            cardTotalInterestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTotalInterestLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardTotalInterestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalInterest)
                    .addComponent(valTotalInterest))
                .addContainerGap(95, Short.MAX_VALUE))
        );
        cardTotalInterestLayout.setVerticalGroup(
            cardTotalInterestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTotalInterestLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTotalInterest)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valTotalInterest)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardRefund60.setBackground(new java.awt.Color(255, 255, 255));
        cardRefund60.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lblRefund60.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblRefund60.setText("Refund 60%");

        valRefund60.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        valRefund60.setText("0.00");

        javax.swing.GroupLayout cardRefund60Layout = new javax.swing.GroupLayout(cardRefund60);
        cardRefund60.setLayout(cardRefund60Layout);
        cardRefund60Layout.setHorizontalGroup(
            cardRefund60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardRefund60Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardRefund60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRefund60)
                    .addComponent(valRefund60))
                .addContainerGap(104, Short.MAX_VALUE))
        );
        cardRefund60Layout.setVerticalGroup(
            cardRefund60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardRefund60Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblRefund60)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valRefund60)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardRefund40.setBackground(new java.awt.Color(255, 255, 255));
        cardRefund40.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lblRefund40.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblRefund40.setText("Refund 40%");

        valRefund40.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        valRefund40.setText("0.00");

        javax.swing.GroupLayout cardRefund40Layout = new javax.swing.GroupLayout(cardRefund40);
        cardRefund40.setLayout(cardRefund40Layout);
        cardRefund40Layout.setHorizontalGroup(
            cardRefund40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardRefund40Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardRefund40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRefund40)
                    .addComponent(valRefund40))
                .addContainerGap(104, Short.MAX_VALUE))
        );
        cardRefund40Layout.setVerticalGroup(
            cardRefund40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardRefund40Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblRefund40)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valRefund40)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardTotalDividend.setBackground(new java.awt.Color(255, 255, 255));
        cardTotalDividend.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lblTotalDividend.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblTotalDividend.setText("Total Dividend");

        valTotalDividend.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        valTotalDividend.setText("0.00");

        javax.swing.GroupLayout cardTotalDividendLayout = new javax.swing.GroupLayout(cardTotalDividend);
        cardTotalDividend.setLayout(cardTotalDividendLayout);
        cardTotalDividendLayout.setHorizontalGroup(
            cardTotalDividendLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTotalDividendLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardTotalDividendLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalDividend)
                    .addComponent(valTotalDividend))
                .addContainerGap(90, Short.MAX_VALUE))
        );
        cardTotalDividendLayout.setVerticalGroup(
            cardTotalDividendLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTotalDividendLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTotalDividend)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valTotalDividend)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dividendTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Total Premium", "Premium %", "Interest", "Refund 60%", "Refund 40%", "Dividend", "Total Dividend"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(dividendTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cardTotalPremium, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cardPremiumPct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cardTotalInterest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cardRefund60, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cardRefund40, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cardTotalDividend, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(40, 40, 40))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cardTotalPremium, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardPremiumPct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardTotalInterest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardRefund60, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardRefund40, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardTotalDividend, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cardPremiumPct;
    private javax.swing.JPanel cardRefund40;
    private javax.swing.JPanel cardRefund60;
    private javax.swing.JPanel cardTotalDividend;
    private javax.swing.JPanel cardTotalInterest;
    private javax.swing.JPanel cardTotalPremium;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JTextField dividendSearchField;
    private javax.swing.JTable dividendTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblPremiumPct;
    private javax.swing.JLabel lblRefund40;
    private javax.swing.JLabel lblRefund60;
    private javax.swing.JLabel lblTotalDividend;
    private javax.swing.JLabel lblTotalInterest;
    private javax.swing.JLabel lblTotalPremium;
    private javax.swing.JComboBox<String> selectServiceCharge;
    private javax.swing.JLabel valPremiumPct;
    private javax.swing.JLabel valRefund40;
    private javax.swing.JLabel valRefund60;
    private javax.swing.JLabel valTotalDividend;
    private javax.swing.JLabel valTotalInterest;
    private javax.swing.JLabel valTotalPremium;
    // End of variables declaration//GEN-END:variables
}
