package com.simon816.rtext.srplugin.memstick;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;

public class LogViewerPanel extends OptionsDialogPanel {
    Pattern logdirRe = Pattern.compile("(\\d{4})-(\\d\\d)-(\\d\\d)");
    Pattern filenameRe = Pattern.compile("(\\d\\d)\\.(\\d\\d)\\.(\\d\\d)\\.log");

    public LogViewerPanel(final File root, RText rtext) {
        super("Log Viewer");
        setLayout(new BorderLayout());
        final ArrayList<String> times = new ArrayList<String>();
        for (File logdir : root.listFiles()) {
            if (logdir.isDirectory()) {
                Matcher dateM = logdirRe.matcher(logdir.getName());
                if (dateM.matches()) {
                    for (File file : logdir.listFiles()) {
                        Matcher m = filenameRe.matcher(file.getName());
                        if (m.matches()) {
                            String time = dateM.group(1) + "/" + dateM.group(2) + "/" + dateM.group(3) + " at " + m.group(1) + ":" + m.group(2) + ":" + m.group(3);
                            times.add(time);
                            addChildPanel(new LogViewPanel(file, time, rtext));
                        }
                    }
                }
            }
        }
        final JList<String> fList = new JList<String>(times.toArray(new String[times.size()]));
        add(new JLabel("The Following log files are available"), BorderLayout.NORTH);
        add(new JScrollPane(fList), BorderLayout.CENTER);
        fList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fList.clearSelection();
            }
        });
        setPreferredSize(new Dimension(600, 300));
    }

    @Override
    protected void doApplyImpl(Frame owner) {
        // TODO Auto-generated method stub

    }

    @Override
    protected OptionsPanelCheckResult ensureValidInputsImpl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JComponent getTopJComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void setValuesImpl(Frame owner) {
        // TODO Auto-generated method stub

    }
}
