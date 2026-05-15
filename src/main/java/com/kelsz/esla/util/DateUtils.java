package com.kelsz.esla.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.sql.Date;

public class DateUtils {

    /**
     * Safely parse a date string from SQLite.
     * Handles:
     * 1. ISO format (YYYY-MM-DD)
     * 2. ISO with time (YYYY-MM-DD HH:MM:SS) - truncated to date
     * 3. Numeric timestamp (milliseconds)
     * 
     * @param dateStr The date string from the database
     * @return java.sql.Date or null if unparseable
     */
    public static java.sql.Date parseSqlDateSafely(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            if (dateStr.matches("\\d+")) {
                return new java.sql.Date(Long.parseLong(dateStr));
            } else {
                if (dateStr.length() > 10) {
                    dateStr = dateStr.substring(0, 10);
                }
                return java.sql.Date.valueOf(dateStr);
            }
        } catch (Exception e) {
            System.err.println("DateUtils: Failed to parse SQL date: " + dateStr);
            return null;
        }
    }

    /**
     * Safely parse a date string from SQLite into LocalDate.
     * 
     * @param dateStr The date string from the database
     * @return LocalDate or null if unparseable
     */
    public static LocalDate parseLocalDateSafely(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            if (dateStr.matches("\\d+")) {
                return Instant.ofEpochMilli(Long.parseLong(dateStr))
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } else {
                if (dateStr.length() > 10) {
                    dateStr = dateStr.substring(0, 10);
                }
                return LocalDate.parse(dateStr);
            }
        } catch (Exception e) {
            System.err.println("DateUtils: Failed to parse LocalDate: " + dateStr);
            return null;
        }
    }
}
