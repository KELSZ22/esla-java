/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package features;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import ui.style;

/**
 *
 * @author kelsz-dev
 */
public class tableEditor extends javax.swing.JPanel implements ui.Refreshable {
    
    private DefaultTableModel tableModel;
    private JTable table;
    private JScrollPane scrollPane;
    
    // Styling options
    private int fontSize = 14;
    private int headerFontSize = 14;
    private Color headerBgColor = new Color(21, 55, 143);
    private Color headerTextColor = new Color(255, 255, 255);
    private Color cellTextColor = new Color(50, 50, 60);
    private Color gridColor = new Color(220, 220, 230);
    private boolean showHorizontalLines = true;
    private boolean showVerticalLines = true;
    private boolean showBorders = true;
    
    @Override
    public void refresh() {
        // Refresh logic if needed
    }

    /**
     * Creates new form tableEditor
     */
    public tableEditor() {
        initComponents();
        setupTable();
        setupUI();
    }
    
    private void setupTable() {
        // Create table model with editable headers
        tableModel = new DefaultTableModel(
            new Object[][] {
                {"Row 1, Col 1", "Row 1, Col 2", "Row 1, Col 3"},
                {"Row 2, Col 1", "Row 2, Col 2", "Row 2, Col 3"},
                {"Row 3, Col 1", "Row 3, Col 2", "Row 3, Col 3"},
                {"Row 4, Col 1", "Row 4, Col 2", "Row 4, Col 3"}
            },
            new Object[] {
                "Column 1", "Column 2", "Column 3"
            }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        
        // Enable header editing
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        
        scrollPane = new JScrollPane(table);
        style.applyScrollStyle(scrollPane);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(style.BACKGROUND);
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(style.BACKGROUND);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));
        
        JLabel titleLabel = new JLabel("TABLE EDITOR");
        style.applyModernLabel(titleLabel, true);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Toolbar panel
        JPanel toolbarPanel = new JPanel(new BorderLayout(20, 0));
        toolbarPanel.setBackground(style.BACKGROUND);
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 15, 40));
        
        // Styling controls panel
        JPanel stylePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        stylePanel.setBackground(style.BACKGROUND);
        
        // Font size control
        JLabel fontSizeLabel = new JLabel("Font Size:");
        fontSizeLabel.setFont(new Font("Ubuntu", Font.PLAIN, 14));
        stylePanel.add(fontSizeLabel);
        
        SpinnerNumberModel fontSizeModel = new SpinnerNumberModel(fontSize, 10, 24, 1);
        JSpinner fontSizeSpinner = new JSpinner(fontSizeModel);
        fontSizeSpinner.setPreferredSize(new Dimension(70, style.FIELD_HEIGHT));
        fontSizeSpinner.addChangeListener(e -> {
            fontSize = (int) fontSizeSpinner.getValue();
            applyTableStyle();
        });
        stylePanel.add(fontSizeSpinner);
        
        // Header font size control
        JLabel headerFontSizeLabel = new JLabel("Header Font:");
        headerFontSizeLabel.setFont(new Font("Ubuntu", Font.PLAIN, 14));
        stylePanel.add(headerFontSizeLabel);
        
        SpinnerNumberModel headerFontSizeModel = new SpinnerNumberModel(headerFontSize, 10, 24, 1);
        JSpinner headerFontSizeSpinner = new JSpinner(headerFontSizeModel);
        headerFontSizeSpinner.setPreferredSize(new Dimension(70, style.FIELD_HEIGHT));
        headerFontSizeSpinner.addChangeListener(e -> {
            headerFontSize = (int) headerFontSizeSpinner.getValue();
            applyTableStyle();
        });
        stylePanel.add(headerFontSizeSpinner);
        
        // Color buttons
        JButton headerBgColorBtn = new JButton("Header BG");
        headerBgColorBtn.setBackground(headerBgColor);
        headerBgColorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Header Background", headerBgColor);
            if (newColor != null) {
                headerBgColor = newColor;
                headerBgColorBtn.setBackground(newColor);
                applyTableStyle();
            }
        });
        stylePanel.add(headerBgColorBtn);
        
        JButton headerTextColorBtn = new JButton("Header Text");
        headerTextColorBtn.setBackground(headerTextColor);
        headerTextColorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Header Text", headerTextColor);
            if (newColor != null) {
                headerTextColor = newColor;
                headerTextColorBtn.setBackground(newColor);
                applyTableStyle();
            }
        });
        stylePanel.add(headerTextColorBtn);
        
        JButton cellTextColorBtn = new JButton("Cell Text");
        cellTextColorBtn.setBackground(cellTextColor);
        cellTextColorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Cell Text", cellTextColor);
            if (newColor != null) {
                cellTextColor = newColor;
                cellTextColorBtn.setBackground(newColor);
                applyTableStyle();
            }
        });
        stylePanel.add(cellTextColorBtn);
        
        // Border options
        JCheckBox horizontalLinesCheck = new JCheckBox("Horizontal Lines", showHorizontalLines);
        horizontalLinesCheck.setFont(new Font("Ubuntu", Font.PLAIN, 14));
        horizontalLinesCheck.addActionListener(e -> {
            showHorizontalLines = horizontalLinesCheck.isSelected();
            applyTableStyle();
        });
        stylePanel.add(horizontalLinesCheck);
        
        JCheckBox verticalLinesCheck = new JCheckBox("Vertical Lines", showVerticalLines);
        verticalLinesCheck.setFont(new Font("Ubuntu", Font.PLAIN, 14));
        verticalLinesCheck.addActionListener(e -> {
            showVerticalLines = verticalLinesCheck.isSelected();
            applyTableStyle();
        });
        stylePanel.add(verticalLinesCheck);
        
        toolbarPanel.add(stylePanel, BorderLayout.WEST);
        
        // Export buttons panel
        JPanel exportPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        exportPanel.setBackground(style.BACKGROUND);
        
        JButton addRowBtn = new JButton("Add Row");
        style.applyButton(addRowBtn);
        addRowBtn.addActionListener(e -> addRow());
        exportPanel.add(addRowBtn);
        
        JButton addColBtn = new JButton("Add Column");
        style.applyButton(addColBtn);
        addColBtn.addActionListener(e -> addColumn());
        exportPanel.add(addColBtn);
        
        JButton exportPdfBtn = new JButton("Export PDF");
        style.applySecondaryButton(exportPdfBtn);
        exportPdfBtn.addActionListener(e -> exportToPDF());
        exportPanel.add(exportPdfBtn);
        
        JButton exportExcelBtn = new JButton("Export Excel");
        style.applySecondaryButton(exportExcelBtn);
        exportExcelBtn.addActionListener(e -> exportToExcel());
        exportPanel.add(exportExcelBtn);
        
        toolbarPanel.add(exportPanel, BorderLayout.EAST);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(style.BACKGROUND);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 40, 40));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(titlePanel, BorderLayout.NORTH);
        add(toolbarPanel, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.SOUTH);
        
        applyTableStyle();
    }
    
    private void applyTableStyle() {
        table.setRowHeight(fontSize + 20);
        table.setFont(new Font("Ubuntu", Font.PLAIN, fontSize));
        table.setForeground(cellTextColor);
        table.setGridColor(gridColor);
        table.setShowHorizontalLines(showHorizontalLines);
        table.setShowVerticalLines(showVerticalLines);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(21, 55, 143));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        
        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Ubuntu", Font.BOLD, headerFontSize));
        header.setBackground(headerBgColor);
        header.setForeground(headerTextColor);
        header.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, headerFontSize + 24));
        
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(String.valueOf(value));
                label.setFont(new Font("Ubuntu", Font.BOLD, headerFontSize));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
                label.setOpaque(true);
                label.setBackground(headerBgColor);
                label.setForeground(headerTextColor);
                return label;
            }
        });
        
        // Add zebra striping
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(248, 250, 255));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        table.revalidate();
        table.repaint();
    }
    
    private void addRow() {
        Object[] newRow = new Object[tableModel.getColumnCount()];
        for (int i = 0; i < newRow.length; i++) {
            newRow[i] = "";
        }
        tableModel.addRow(newRow);
    }
    
    private void addColumn() {
        int newColIndex = tableModel.getColumnCount();
        tableModel.addColumn("Column " + (newColIndex + 1));
        
        // Fill new column with empty values
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("", i, newColIndex);
        }
    }
    
    private void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF");
        fileChooser.setSelectedFile(new File("table_export.pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);
                
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                
                float margin = 50;
                float yPosition = 750;
                float rowHeight = 25;
                
                // Calculate column widths based on content
                int colCount = tableModel.getColumnCount();
                float[] colWidths = new float[colCount];
                float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
                
                // Equal width distribution
                float defaultColWidth = tableWidth / colCount;
                for (int i = 0; i < colCount; i++) {
                    colWidths[i] = defaultColWidth;
                }
                
                // Draw title
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(margin, 780);
                contentStream.showText("Table Export");
                contentStream.endText();
                
                // Draw headers
                contentStream.setLineWidth(1f);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, headerFontSize);
                
                // Header background
                contentStream.setNonStrokingColor(
                    headerBgColor.getRed() / 255f,
                    headerBgColor.getGreen() / 255f,
                    headerBgColor.getBlue() / 255f
                );
                contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                contentStream.fill();
                
                // Header text
                contentStream.setNonStrokingColor(
                    headerTextColor.getRed() / 255f,
                    headerTextColor.getGreen() / 255f,
                    headerTextColor.getBlue() / 255f
                );
                
                float xPosition = margin;
                for (int col = 0; col < colCount; col++) {
                    String headerText = tableModel.getColumnName(col);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition + 10, yPosition - 7);
                    contentStream.showText(headerText.length() > 30 ? headerText.substring(0, 30) : headerText);
                    contentStream.endText();
                    xPosition += colWidths[col];
                }
                
                yPosition -= rowHeight;
                
                // Draw data rows
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.setNonStrokingColor(
                    cellTextColor.getRed() / 255f,
                    cellTextColor.getGreen() / 255f,
                    cellTextColor.getBlue() / 255f
                );
                
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    // Row border
                    if (showBorders) {
                        contentStream.setStrokingColor(
                            gridColor.getRed() / 255f,
                            gridColor.getGreen() / 255f,
                            gridColor.getBlue() / 255f
                        );
                        contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                        contentStream.stroke();
                    }
                    
                    xPosition = margin;
                    for (int col = 0; col < colCount; col++) {
                        Object value = tableModel.getValueAt(row, col);
                        String stringValue = value != null ? value.toString() : "";
                        contentStream.beginText();
                        contentStream.newLineAtOffset(xPosition + 10, yPosition - 7);
                        contentStream.showText(stringValue.length() > 30 ? stringValue.substring(0, 30) : stringValue);
                        contentStream.endText();
                        xPosition += colWidths[col];
                    }
                    
                    yPosition -= rowHeight;
                    
                    // New page if needed
                    if (yPosition < margin + rowHeight) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = 750;
                    }
                }
                
                contentStream.close();
                document.save(file);
                
                JOptionPane.showMessageDialog(this, "PDF exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel");
        fileChooser.setSelectedFile(new File("table_export.xlsx"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Table Export");
                
                // Create header style
                CellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setFontHeightInPoints((short) headerFontSize);
                headerStyle.setFont(headerFont);
                
                // Set header background color
                byte[] headerRgb = new byte[] {
                    (byte) headerBgColor.getRed(),
                    (byte) headerBgColor.getGreen(),
                    (byte) headerBgColor.getBlue()
                };
                headerStyle.setFillForegroundColor(new XSSFColor(headerRgb, null));
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                
                // Set header text color (using indexed color for compatibility)
                headerFont.setColor(IndexedColors.BLACK.getIndex());
                
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);
                
                // Create cell style
                CellStyle cellStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font cellFont = workbook.createFont();
                cellFont.setFontHeightInPoints((short) fontSize);
                cellStyle.setFont(cellFont);
                
                // Set cell text color (using indexed color for compatibility)
                cellFont.setColor(IndexedColors.BLACK.getIndex());
                
                if (showBorders) {
                    cellStyle.setBorderTop(BorderStyle.THIN);
                    cellStyle.setBorderBottom(BorderStyle.THIN);
                    cellStyle.setBorderLeft(BorderStyle.THIN);
                    cellStyle.setBorderRight(BorderStyle.THIN);
                }
                
                // Write headers
                Row headerRow = sheet.createRow(0);
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(tableModel.getColumnName(col));
                    cell.setCellStyle(headerStyle);
                }
                
                // Write data rows
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    Row excelRow = sheet.createRow(row + 1);
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Cell cell = excelRow.createCell(col);
                        Object value = tableModel.getValueAt(row, col);
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        }
                        cell.setCellStyle(cellStyle);
                    }
                }
                
                // Auto-size columns
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    sheet.autoSizeColumn(col);
                }
                
                // Write to file
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    workbook.write(outputStream);
                }
                
                JOptionPane.showMessageDialog(this, "Excel exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
