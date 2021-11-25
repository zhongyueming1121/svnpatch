package com.ymz.ui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * 限制行数的输出
 * @author: ymz
 * @date: 2021-08-21 04:22
 **/
public class MyJTextArea extends JTextArea {
    int limitLine = 500;
    public MyJTextArea() {
        super();
    }

    public MyJTextArea(String text) {
        super(text);
    }

    public MyJTextArea(int rows, int columns) {
        super(rows, columns);
    }

    public MyJTextArea(String text, int rows, int columns) {
        super(text, rows, columns);
    }

    public MyJTextArea(Document doc) {
        super(doc);
    }

    public MyJTextArea(Document doc, String text, int rows, int columns) {
        super(doc, text, rows, columns);
    }

    /**
     * Appends the given text to the end of the document.  Does nothing if
     * the model is null or the string is null or empty.
     *
     * @param str the text to insert
     * @see #insert
     */
    @Override
    public void append(String str) {
        if(limitLine<20){
            limitLine = 20;
        }
        Document doc = getDocument();
        if (doc != null) {
            try {
                int lineCount = getLineCount();
                if (lineCount > limitLine) {
                    int lineStartOffset = getLineStartOffset(0);
                    int lineEndOffset = getLineEndOffset(10);
                    doc.remove(lineStartOffset, lineEndOffset);
                }
                doc.insertString(doc.getLength(), str, null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
}
