package com.kelsz.esla.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import ui.style;

public class ReportExporter {

    public static void exportToExcel(JTable table, String title, String subtitle, int[] visibleColumns) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as Excel");
        fileChooser.setSelectedFile(new File(title.toLowerCase().replace(" ", "_") + "_" + LocalDate.now() + ".xlsx"));

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Report");

                // Styles
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);

                CellStyle groupStyle = workbook.createCellStyle();
                Font groupFont = workbook.createFont();
                groupFont.setBold(true);
                groupStyle.setFont(groupFont);
                groupStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                groupStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                CellStyle currencyStyle = workbook.createCellStyle();
                currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

                // Title and Subtitle
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(title.toUpperCase());
                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 16);
                CellStyle titleStyle = workbook.createCellStyle();
                titleStyle.setFont(titleFont);
                titleCell.setCellStyle(titleStyle);

                Row subtitleRow = sheet.createRow(1);
                subtitleRow.createCell(0).setCellValue(subtitle);

                // Headers
                Row headerRow = sheet.createRow(3);
                for (int i = 0; i < visibleColumns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(table.getColumnName(visibleColumns[i]));
                    cell.setCellStyle(headerStyle);
                }

                // Data
                int rowIdx = 4;
                for (int i = 0; i < table.getRowCount(); i++) {
                    Row row = sheet.createRow(rowIdx++);
                    boolean isHeader = isGroupHeader(table, i);

                    for (int j = 0; j < visibleColumns.length; j++) {
                        Cell cell = row.createCell(j);
                        Object value = table.getValueAt(i, visibleColumns[j]); 

                        if (value != null) {
                            if (value instanceof BigDecimal) {
                                cell.setCellValue(((BigDecimal) value).doubleValue());
                                if (!isHeader) cell.setCellStyle(currencyStyle);
                            } else if (value instanceof Number) {
                                cell.setCellValue(((Number) value).doubleValue());
                            } else {
                                String strValue = value.toString();
                                cell.setCellValue(strValue);
                            }
                        }

                        if (isHeader) {
                            cell.setCellStyle(groupStyle);
                        }
                    }
                }

                // Auto-size columns
                for (int i = 0; i < visibleColumns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(fileChooser.getSelectedFile())) {
                    workbook.write(fileOut);
                    style.showMessageDialog(null, "Excel report generated successfully!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                style.showMessageDialog(null, "Error generating Excel: " + e.getMessage());
            }
        }
    }

    public static void exportToPDF(JTable table, String title, String subtitle, int[] visibleColumns) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as PDF");
        fileChooser.setSelectedFile(new File(title.toLowerCase().replace(" ", "_") + "_" + LocalDate.now() + ".pdf"));

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try (PDDocument document = new PDDocument()) {
                // Dynamic Orientation: Use Landscape if many columns
                boolean landscape = visibleColumns.length > 7;
                PDRectangle pageSize = landscape ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()) : PDRectangle.A4;
                PDPage page = new PDPage(pageSize);
                document.addPage(page);
                
                // Dynamic Scaling: Adjust font size based on column count
                float fontSize = visibleColumns.length > 12 ? 6 : (visibleColumns.length > 9 ? 7 : (visibleColumns.length > 7 ? 8 : 9));
                float headerFontSize = fontSize + 1;
                float rowHeight = fontSize + 10;

                float margin = 40;
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float tableWidth = pageWidth - (2 * margin);
                float yStart = pageHeight - margin;
                float yPosition = yStart;

                PDPageContentStream contentStream = new PDPageContentStream(document, page);

                // Title
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(title.toUpperCase());
                contentStream.endText();
                yPosition -= 25;

                // Subtitle
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(subtitle);
                contentStream.endText();
                yPosition -= 30;

                // Column Widths proportional to JTable column widths
                float totalJTableWidth = 0;
                for (int colIdx : visibleColumns) {
                    totalJTableWidth += table.getColumnModel().getColumn(colIdx).getWidth();
                }

                float[] colWidths = new float[visibleColumns.length];
                for (int i = 0; i < visibleColumns.length; i++) {
                    float jTableColWidth = table.getColumnModel().getColumn(visibleColumns[i]).getWidth();
                    colWidths[i] = (jTableColWidth / totalJTableWidth) * tableWidth;
                }

                // Draw Headers
                contentStream.setNonStrokingColor(66, 133, 244); // Blue
                contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                contentStream.fill();
                contentStream.setNonStrokingColor(255, 255, 255); // White text

                float xPosition = margin;
                for (int i = 0; i < visibleColumns.length; i++) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, headerFontSize);
                    
                    String header = table.getColumnName(visibleColumns[i]);
                    // Truncate header if too long for column width
                    float maxHeaderWidth = colWidths[i] - 10;
                    try {
                        while (header.length() > 3 && PDType1Font.HELVETICA_BOLD.getStringWidth(header) / 1000 * headerFontSize > maxHeaderWidth) {
                            header = header.substring(0, header.length() - 1);
                        }
                    } catch (Exception e) {
                        if (header.length() > 10) header = header.substring(0, 7) + "...";
                    }
                    
                    contentStream.newLineAtOffset(xPosition + 5, yPosition - (rowHeight * 0.75f));
                    contentStream.showText(header);
                    contentStream.endText();
                    xPosition += colWidths[i];
                }

                yPosition -= rowHeight;
                contentStream.setNonStrokingColor(0, 0, 0); // Black text

                // Draw Rows
                for (int i = 0; i < table.getRowCount(); i++) {
                    if (yPosition < margin + rowHeight) {
                        contentStream.close();
                        page = new PDPage(pageSize);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                        
                        // Re-draw table header for new page (optional but better)
                        contentStream.setNonStrokingColor(66, 133, 244);
                        contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                        contentStream.fill();
                        contentStream.setNonStrokingColor(255, 255, 255);
                        float headerX = margin;
                        for (int j = 0; j < visibleColumns.length; j++) {
                            contentStream.beginText();
                            contentStream.setFont(PDType1Font.HELVETICA_BOLD, headerFontSize);
                            String headerText = table.getColumnName(visibleColumns[j]);
                            float maxWidth = colWidths[j] - 10;
                            try {
                                while (headerText.length() > 3 && PDType1Font.HELVETICA_BOLD.getStringWidth(headerText) / 1000 * headerFontSize > maxWidth) {
                                    headerText = headerText.substring(0, headerText.length() - 1);
                                }
                            } catch (Exception e) {}
                            contentStream.newLineAtOffset(headerX + 5, yPosition - (rowHeight * 0.75f));
                            contentStream.showText(headerText);
                            contentStream.endText();
                            headerX += colWidths[j];
                        }
                        yPosition -= rowHeight;
                        contentStream.setNonStrokingColor(0, 0, 0);
                    }

                    boolean isHeader = isGroupHeader(table, i);
                    if (isHeader) {
                        contentStream.setNonStrokingColor(240, 240, 240);
                        contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                        contentStream.fill();
                        contentStream.setNonStrokingColor(0, 0, 0);
                    }

                    // Draw Horizontal Line
                    contentStream.setLineWidth(0.5f);
                    contentStream.moveTo(margin, yPosition);
                    contentStream.lineTo(margin + tableWidth, yPosition);
                    contentStream.stroke();

                    xPosition = margin;
                    for (int j = 0; j < visibleColumns.length; j++) {
                        // Draw Vertical Line
                        contentStream.moveTo(xPosition, yPosition);
                        contentStream.lineTo(xPosition, yPosition - rowHeight);
                        contentStream.stroke();

                        Object value = table.getValueAt(i, visibleColumns[j]);
                        String text = (value != null) ? value.toString() : "";
                        
                        // Intelligent Truncation based on available width
                        float maxWidth = colWidths[j] - 10;
                        try {
                            while (text.length() > 3 && (isHeader ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA).getStringWidth(text) / 1000 * fontSize > maxWidth) {
                                text = text.substring(0, text.length() - 1);
                            }
                        } catch (Exception e) {}

                        contentStream.beginText();
                        contentStream.setFont(isHeader ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, fontSize);
                        
                        // Right-align numbers
                        float textWidth = (isHeader ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA).getStringWidth(text) / 1000 * fontSize;
                        float xOffset = 5;
                        if (value instanceof Number || (text.contains(".") && text.length() < 15)) {
                            xOffset = colWidths[j] - textWidth - 5;
                        }
                        
                        contentStream.newLineAtOffset(xPosition + xOffset, yPosition - (rowHeight * 0.75f));
                        try {
                            contentStream.showText(text);
                        } catch (Exception e) {
                            contentStream.showText("?");
                        }
                        contentStream.endText();
                        xPosition += colWidths[j];
                    }
                    
                    // Final vertical line
                    contentStream.moveTo(xPosition, yPosition);
                    contentStream.lineTo(xPosition, yPosition - rowHeight);
                    contentStream.stroke();

                    yPosition -= rowHeight;
                }

                // Final bottom line
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(margin + tableWidth, yPosition);
                contentStream.stroke();

                contentStream.close();
                document.save(fileChooser.getSelectedFile());
                style.showMessageDialog(null, "PDF report generated successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                style.showMessageDialog(null, "Error generating PDF: " + e.getMessage());
            }
        }
    }

    private static boolean isGroupHeader(JTable table, int row) {
        Object value = table.getValueAt(row, 0);
        if (value == null) return false;
        String str = value.toString().trim();
        if (str.isEmpty()) return false;
        
        // Group headers are usually uppercase names or "TOTAL"
        // Also check if other columns in this row are mostly empty
        boolean othersEmpty = true;
        for (int i = 1; i < Math.min(table.getColumnCount(), 5); i++) {
            Object val = table.getValueAt(row, i);
            if (val != null && !val.toString().trim().isEmpty()) {
                othersEmpty = false;
                break;
            }
        }
        
        return (str.equals(str.toUpperCase()) || str.equals("TOTAL")) && othersEmpty;
    }
}
