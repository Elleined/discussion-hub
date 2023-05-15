package com.forum.application.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface Formatter {
    static String formatDate(LocalDate orderDate) {
        String month = orderDate.getMonth().name();
        String finalMonth = month
                .substring(0, 1)
                .toUpperCase() +
                month.substring(1).toLowerCase();

        String day = String.valueOf(orderDate.getDayOfMonth());
        String year = String.valueOf(orderDate.getYear());
        return String.format("%s %s, %s", finalMonth, day, year);
    }

    static String formatDate(LocalDateTime orderDate) {
        String month = orderDate.getMonth().name();
        String finalMonth = month
                .substring(0, 1)
                .toUpperCase() +
                month.substring(1).toLowerCase();

        String day = String.valueOf(orderDate.getDayOfMonth());
        String year = String.valueOf(orderDate.getYear());
        return String.format("%s %s, %s", finalMonth, day, year);
    }

    static String formatTime(LocalDateTime orderDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return formatter.format(orderDate);
    }

    static String formatString(String field) {
        return field.substring(0, 1).toUpperCase() +
                field.substring(1).toLowerCase();
    }
}
