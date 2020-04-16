package com.chess.gui;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TextAreaOutputStream extends OutputStream {

    private final JTextArea textArea;
    private final StringBuilder sb = new StringBuilder();

    public TextAreaOutputStream(final JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public void write(int b) throws IOException {

        if (b == '\r')
            return;

        if (b == '\n') {
            final String text;
            if(textArea.getText().equals("")){
                text=sb.toString();
            }
            else{
                text = "\n"+sb.toString();
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textArea.append(text);
                }
            });
            sb.setLength(0);
            return;
        }

        sb.append((char) b);
    }
}