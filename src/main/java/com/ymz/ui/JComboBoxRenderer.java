package com.ymz.ui;

import java.awt.*;

import javax.swing.JComboBox;

import javax.swing.JList;

import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * 下拉悬浮框
 *
 * @author: ymz
 * @date: 2021-08-21 04:22
 */
public class JComboBoxRenderer extends BasicComboBoxRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            if (-1 < index) {
                String show = value == null ? "" : value.toString();
                if (show.length() > 150) {
                    show = show.substring(150) + "...";
                }
                list.setToolTipText((value == null) ? null : show);
            }
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setFont(list.getFont());
        setText(value == null ? "" : value.toString());
        return this;
    }

}


