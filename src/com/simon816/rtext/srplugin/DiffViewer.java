package com.simon816.rtext.srplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.fife.rsta.ui.EscapableDialog;
import org.fife.ui.ResizableFrameContentPane;

public class DiffViewer extends EscapableDialog {

    public DiffViewer(String diff) {
        super();
        JPanel cp = new ResizableFrameContentPane(new BorderLayout());
        setContentPane(cp);
        String html = computeHTML(diff);
        JEditorPane editor = new JEditorPane("text/html", html) {
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        editor.setEditable(false);
        cp.setSize(640, 480);
        cp.setMaximumSize(new Dimension(800, 600));
        JScrollPane scroll = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setMaximumSize(new Dimension(800, 600));
        cp.add(scroll, BorderLayout.CENTER);
        setSize(640, 480);
        setMaximumSize(new Dimension(800, 600));
        pack();
    }

    private String computeHTML(String diff) {
        if (diff == null || diff.isEmpty()) {
            return "";
        }
        StringBuilder html = new StringBuilder();
        String[] difflines = diff.replace("\r", "").split("\n");
        html.append("<pre>");
        for (String line : difflines) {
            html.append("<font color=\"");
            String color;
            switch (line.charAt(0)) {
            case '+':
                color = "green";
                break;
            case '-':
                color = "red";
                break;
            case '@':
                color = "orange";
                break;
            default:
                color = "black";
                break;
            }
            html.append(color + "\">");
            html.append(line);
            html.append("</font><br>");
        }
        return html.toString() + "</pre>";
    }
}
