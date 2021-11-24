package com.ymz.ui;


import javax.swing.*;
import java.text.DateFormat;
import java.util.Date;

class DateTimePickerTest {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("Date Time Picker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setFormats(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM));
        dateTimePicker.setTimeFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM));
        dateTimePicker.setDate(new Date());

        frame.getContentPane().add(dateTimePicker);
        frame.pack();
        frame.setVisible(true);
    }

}